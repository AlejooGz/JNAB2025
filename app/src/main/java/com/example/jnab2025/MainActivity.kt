package com.example.jnab2025

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.example.jnab2025.notificaciones.NotificacionesWorker
import com.example.jnab2025.notificaciones.Notificaciones
import com.example.jnab2025.notificaciones.ProgramadorRecordatorios
import com.example.jnab2025.notificaciones.RegistroNotificaciones
import com.example.jnab2025.ui.viewmodels.NotificacionesViewModel
import com.example.jnab2025.databinding.ActivityMainBinding
import com.example.jnab2025.utils.Sesion
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.example.jnab2025.data.firebase.FirebaseSeed
import com.example.jnab2025.data.model.TipoNotificacion
import com.example.jnab2025.ui.fragments.MapsFragment
import com.example.jnab2025.ui.viewmodels.FiltroViewModel

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    private lateinit var notificacionesViewModel: NotificacionesViewModel

    /** Ultimo usuario para el que ya se hizo la puesta al dia de avisos. */
    private var uidSincronizado: String? = null

    /** Destino actual del NavController, para decidir si va la campanita. */
    private var destinoActual: Int? = null

    /** Globito de la campanita; existe recien despues de onCreateOptionsMenu. */
    private var tvBadgeNotificaciones: TextView? = null

    /**
     * Lugar de una notificacion LUGAR_AGREGADO tocada en la bandeja del
     * telefono. Se guarda hasta que haya sesion (en frio se arranca en el
     * login) y ahi se navega al mapa.
     */
    private var lugarPendiente: String? = null

    private val pedirPermisoNotificaciones =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { /* Si lo rechaza, la app sigue andando: simplemente no avisa. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prepararNotificaciones()

        // Al recrearse (rotacion, etc.) el intent es el mismo: ya se atendio.
        if (savedInstanceState == null) {
            leerNotificacionTocada(intent)
        }

        // Configurar Toolbar
        setSupportActionBar(binding.toolbar)

        // Configurar Drawer Toggle
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Listener para los ítems del NavigationView
        binding.navView.setNavigationItemSelectedListener(this)

        // Mostrar el nombre del usuario logueado y el menú que le corresponde
        refrescarSesionEnUi()

        // Configurar navegación con BottomNavigationView
        binding.navHostFragment.post {
            val navController = findNavController(R.id.nav_host_fragment)

            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.setupWithNavController(navController)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                destinoActual = destination.id
                // onPrepareOptionsMenu decide si la campanita va en este destino
                invalidateOptionsMenu()

                when (destination.id) {
                    R.id.loginFragment,
                    R.id.registroFragment -> {
                        binding.toolbar.visibility = View.GONE
                        binding.navView.visibility = View.GONE
                        bottomNav.visibility = View.GONE
                        binding.drawerLayout.setDrawerLockMode(
                            DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                        )
                    }
                    else -> {
                        binding.toolbar.visibility = View.VISIBLE
                        binding.navView.visibility = View.VISIBLE
                        bottomNav.visibility = View.VISIBLE
                        binding.drawerLayout.setDrawerLockMode(
                            DrawerLayout.LOCK_MODE_UNLOCKED
                        )
                        refrescarSesionEnUi()
                        sincronizarNotificacionesConSesion()
                        // se difiere para no navegar desde dentro del listener
                        binding.navHostFragment.post { abrirLugarPendiente() }
                    }
                }
            }
        }
        // Código comentado que antes redirigía al login:
        /*
        if (isLoggedIn) {
            startActivity(Intent(this, LoginFragment::class.java))
            finish()
            return
        }
        */
    }

    /** Con la app abierta, tocar un aviso llega aca (el intent es SINGLE_TOP). */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        leerNotificacionTocada(intent)
        abrirLugarPendiente()
    }

    /**
     * Anota a donde hay que ir si el intent viene de tocar una notificacion.
     * Hoy solo LUGAR_AGREGADO lleva a una pantalla puntual; el resto abre la
     * app donde estaba.
     */
    private fun leerNotificacionTocada(intent: Intent?) {
        if (intent == null) return
        // Abrir desde "recientes" vuelve a entregar el ultimo intent: no es un toque nuevo.
        if (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return

        val tipo = intent.getStringExtra(Notificaciones.EXTRA_TIPO)
        val referenciaId = intent.getStringExtra(Notificaciones.EXTRA_REFERENCIA_ID)
        intent.removeExtra(Notificaciones.EXTRA_TIPO)
        intent.removeExtra(Notificaciones.EXTRA_REFERENCIA_ID)

        if (tipo == TipoNotificacion.LUGAR_AGREGADO.name && !referenciaId.isNullOrBlank()) {
            lugarPendiente = referenciaId
        }
    }

    /**
     * Navega al mapa centrado en el lugar pendiente, si lo hay. En el login o
     * el registro todavia no hay sesion: se espera al siguiente destino.
     */
    private fun abrirLugarPendiente() {
        val lugarId = lugarPendiente ?: return
        val navController = findNavController(R.id.nav_host_fragment)
        val destino = navController.currentDestination?.id ?: return
        if (destino == R.id.loginFragment || destino == R.id.registroFragment) return

        lugarPendiente = null
        navController.navigate(
            R.id.mapsFragment,
            bundleOf(MapsFragment.ARG_LUGAR_ID to lugarId),
            // si ya estaba en el mapa, se reemplaza en vez de apilar otro
            NavOptions.Builder().setLaunchSingleTop(true).build()
        )
    }

    /**
     * Deja lista la infraestructura de avisos: canales, permiso en Android 13+,
     * el listener en vivo y el worker que cubre a la app cerrada.
     */
    private fun prepararNotificaciones() {
        Notificaciones.crearCanales(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!concedido) {
                pedirPermisoNotificaciones.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }
        }

        notificacionesViewModel =
            ViewModelProvider(this)[NotificacionesViewModel::class.java]

        // El globito de la campanita sigue en vivo a las no leidas.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                notificacionesViewModel.noLeidas.collect { actualizarBadge(it) }
            }
        }

        NotificacionesWorker.programarPeriodico(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_notificaciones, menu)

        // Con actionLayout el toque lo recibe la vista propia, no llega solo a
        // onOptionsItemSelected: hay que reenviarlo.
        val item = menu.findItem(R.id.action_notificaciones)
        val vista = item.actionView
        vista?.setOnClickListener { onOptionsItemSelected(item) }
        tvBadgeNotificaciones = vista?.findViewById(R.id.tvBadgeNotificaciones)
        actualizarBadge(notificacionesViewModel.noLeidas.value)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_notificaciones)?.isVisible = mostrarCampanita()
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_notificaciones) {
            findNavController(R.id.nav_host_fragment)
                .navigate(R.id.notificacionesFragment)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Solo en el home, y solo si ese home es el de expositor o asistente: al
     * organizador no le llegan avisos (es quien aprueba pagos y fija horarios).
     * Replica el criterio de MainFragment.mostrarHomeSegunRol().
     */
    private fun mostrarCampanita(): Boolean =
        destinoActual == R.id.mainFragment &&
                !Sesion.esOrganizador(this) &&
                (Sesion.esExpositor(this) || Sesion.esAsistente(this))

    private fun actualizarBadge(noLeidas: Int) {
        val badge = tvBadgeNotificaciones ?: return
        badge.visibility = if (noLeidas > 0) View.VISIBLE else View.GONE
        badge.text = if (noLeidas > 9) "9+" else noLeidas.toString()
    }

    /**
     * Engancha los avisos al usuario actual. Se llama en cada cambio de
     * destino porque el login ocurre dentro de un fragment: recien ahi se sabe
     * que hay sesion y con que UID.
     */
    private fun sincronizarNotificacionesConSesion() {
        notificacionesViewModel.escuchar()

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (uid == uidSincronizado) return
        uidSincronizado = uid

        // Puesta al dia al entrar: avisos que hayan quedado pendientes y
        // alarmas de charla que se hayan perdido (reinicio, cierre forzado).
        NotificacionesWorker.ejecutarAhora(this)
    }

    /**
     * Muestra u oculta la capa de "cargando" que tapa toda la pantalla mientras
     * un fragment espera la respuesta de Firebase. Tambien traba el drawer para
     * que no se pueda navegar a mitad de un envio.
     */
    fun mostrarCargando(visible: Boolean, mensaje: String = "Enviando…") {
        val capa = binding.capaCargando
        if (visible) {
            binding.tvCargando.text = mensaje
            capa.visibility = View.VISIBLE
            binding.drawerLayout.setDrawerLockMode(
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED
            )
        } else if (capa.visibility == View.VISIBLE) {
            capa.visibility = View.GONE
            // No se restaura un modo guardado: el envio pudo terminar navegando
            // (login -> main), asi que se decide segun donde se esta ahora.
            val destino =
                findNavController(R.id.nav_host_fragment).currentDestination?.id
            val sinDrawer =
                destino == R.id.loginFragment || destino == R.id.registroFragment
            binding.drawerLayout.setDrawerLockMode(
                if (sinDrawer) DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                else DrawerLayout.LOCK_MODE_UNLOCKED
            )
        }
    }

    private fun refrescarSesionEnUi() {
        val headerView = binding.navView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.tvDrawerUsername).text = Sesion.nombre(this)

        val navMenu = binding.navView.menu
        navMenu.setGroupVisible(R.id.group_expositor, Sesion.esExpositor(this))
        navMenu.setGroupVisible(R.id.group_admin, Sesion.esOrganizador(this))
        navMenu.setGroupVisible(
            R.id.group_asistente,
            Sesion.esAsistente(this) ||
                    Sesion.esExpositor(this)
        )
        navMenu.findItem(R.id.nav_simposios)?.isVisible = true

        Log.d("Sesion", "uid=${Sesion.firebaseUid(this)} roles=${Sesion.roles(this)}")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_perfil -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.perfilFragment)
            }
            R.id.nav_simposios -> {
                if (findNavController(R.id.nav_host_fragment).currentDestination?.id != R.id.mainFragment) {
                    findNavController(R.id.nav_host_fragment).popBackStack(R.id.mainFragment, false)
                }
                findNavController(R.id.nav_host_fragment).navigate(R.id.action_mainFragment_to_simposiosFragment)
            }
            R.id.nav_enviar_trabajo -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.simposiosTramiteFragment)
            }
            R.id.nav_mis_trabajos -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.seguimientoTramiteFragment)
            }
            R.id.nav_mis_simposios -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.misSimposiosFragment)
            }
            R.id.nav_ver_inscriptos -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.verInscriptosFragment)
            }
            R.id.nav_inscripcion -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.inscripcionFragment)
            }
            R.id.nav_logout -> {
                // Los avisos y las alarmas son del usuario que se va: si no se
                // limpian, el proximo que entre en este telefono hereda sus
                // recordatorios de charla.
                ProgramadorRecordatorios.cancelarTodos(this)
                RegistroNotificaciones.limpiar(this)
                NotificacionesWorker.cancelar(this)
                uidSincronizado = null

                // limpia los filtros del mapa antes de cerrar la sesión
                val filtroViewModel = ViewModelProvider(this)[FiltroViewModel::class.java]
                filtroViewModel.limpiarFiltros()
                // limpia la sesión local
                Sesion.cerrar(this)
                // cierra sesión en Firebase Authentication
                FirebaseAuth.getInstance().signOut()
                // limpia preferencias locales de la app
                getSharedPreferences(
                    "AppPreferences",
                    Context.MODE_PRIVATE
                )
                    .edit().clear().apply()
                //vuelve al login eliminando el historial de navegación
                findNavController(R.id.nav_host_fragment
                ).navigate(
                    R.id.loginFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(
                            R.id.nav_graph,
                            true
                        )
                        .build()
                )
            }
            R.id.nav_crear_novedad -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.crearNovedadFragment)
            }
            R.id.nav_gestionar_faq -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.gestionFaqFragment)
            }
            R.id.nav_gestionar_lugares -> {
                findNavController(R.id.nav_host_fragment).navigate(R.id.gestionLugaresFragment)
            }
        }

        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}