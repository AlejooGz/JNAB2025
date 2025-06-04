package com.example.jnab2025

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.jnab2025.databinding.ActivityMainBinding
import com.example.jnab2025.ui.fragments.LoginFragment
import com.example.jnab2025.ui.viewmodels.SimposioViewModel
import com.example.jnab2025.utils.SesionUsuario
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    // Variable para la carga previa de simposios de prueba (de tu compañero)
    private val simposioViewModel: SimposioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Solo para pruebas: reinicia los datos al abrir la app (SimposioViewModel)
        simposioViewModel.reiniciarConDatosDeEjemplo()

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

        // Mostrar el nombre del usuario logueado en el header
        val headerView = binding.navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.tvDrawerUsername)

        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", "Invitado")
        usernameTextView.text = username

        val isLoggedIn = !sharedPref.getString("username", null).isNullOrEmpty()
        Log.d("DEBUG", "Username: $username")
        Log.d("DEBUG", "isLoggedIn: $isLoggedIn")

        // Configurar visibilidad del menú según el rol
        configurarMenuPorRol()

        // Configurar navegación con BottomNavigationView
        binding.navHostFragment.post {
            val navController = findNavController(R.id.nav_host_fragment)

            val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNav.setupWithNavController(navController)

            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.loginFragment -> {
                        binding.toolbar.visibility = View.GONE
                        binding.navView.visibility = View.GONE
                        bottomNav.visibility = View.GONE
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }
                    else -> {
                        binding.toolbar.visibility = View.VISIBLE
                        binding.navView.visibility = View.VISIBLE
                        bottomNav.visibility = View.VISIBLE
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
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

    private fun configurarMenuPorRol() {
        val rol = SesionUsuario.obtenerRol(this)
        Log.d("DEBUG", "Rol del usuario en MainActivity: $rol")

        val navMenu = binding.navView.menu

        // Ocultar todos los grupos primero
        navMenu.setGroupVisible(R.id.group_expositor, false)
        navMenu.setGroupVisible(R.id.group_admin, false)
        navMenu.setGroupVisible(R.id.group_asistente, false)

        // Mostrar el grupo correspondiente al rol
        when {
            SesionUsuario.esExpositor(this) -> navMenu.setGroupVisible(R.id.group_expositor, true)
            SesionUsuario.esOrganizador(this) -> navMenu.setGroupVisible(R.id.group_admin, true)
            SesionUsuario.esAsistente(this) -> navMenu.setGroupVisible(R.id.group_asistente, true)
        }

        // Mostrar siempre Simposios
        navMenu.findItem(R.id.nav_simposios)?.isVisible = true
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
                findNavController(R.id.nav_host_fragment).navigate(R.id.tramiteExpositorFragment)
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
                val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                sharedPref.edit().clear().apply()

                // Navegar al login usando la acción global
                findNavController(R.id.nav_host_fragment).navigate(
                    R.id.loginFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build()
                )
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