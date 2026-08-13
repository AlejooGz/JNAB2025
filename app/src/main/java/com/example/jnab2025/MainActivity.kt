package com.example.jnab2025

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.jnab2025.databinding.ActivityMainBinding
import com.example.jnab2025.utils.Sesion
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.jnab2025.data.SimposioFakeData
import com.example.jnab2025.data.UserFakeData
import com.example.jnab2025.data.CharlaFakeData
import com.example.jnab2025.data.db.AppDatabase
import com.example.jnab2025.data.local.JnabDatabase
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prepararDatos()

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
                        // Al salir del login la sesión recién existe: hay que
                        // rearmar el menú sin reiniciar la Activity.
                        refrescarSesionEnUi()
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

    /**
     * Siembra la base vieja una sola vez (las pantallas sin migrar todavía la
     * usan) y fuerza la creación de la base nueva para que corra su seed.
     *
     * Antes esto borraba y reinsertaba todo en cada arranque, con seis corrutinas
     * sin orden garantizado: se perdían los datos del usuario y podía fallar por
     * foreign key. Ahora es secuencial y sólo corre si está vacía.
     */
    private fun prepararDatos() {
        lifecycleScope.launch {
            val vieja = AppDatabase.getDatabase(this@MainActivity)
            if (vieja.userDao().obtenerTodos().isEmpty()) {
                vieja.userDao().insertarTodos(UserFakeData.getUsersDeEjemplo())
                vieja.simposioDao().insertarTodos(SimposioFakeData.getSimposiosDeEjemplo())
                vieja.charlaDao().insertarTodos(CharlaFakeData.getCharlasDeEjemplo())
            }

            // La primera consulta crea jnab.db y dispara SeedJnab.
            JnabDatabase.get(this@MainActivity).eventoDao().actual()
        }
    }

    private fun refrescarSesionEnUi() {
        val headerView = binding.navView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.tvDrawerUsername).text = Sesion.nombre(this)

        val navMenu = binding.navView.menu
        navMenu.setGroupVisible(R.id.group_expositor, Sesion.esExpositor(this))
        navMenu.setGroupVisible(R.id.group_admin, Sesion.esOrganizador(this))
        navMenu.setGroupVisible(R.id.group_asistente, Sesion.esAsistente(this))
        navMenu.findItem(R.id.nav_simposios)?.isVisible = true

        Log.d("Sesion", "usuario=${Sesion.usuarioId(this)} roles=${Sesion.roles(this)}")
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
                Sesion.cerrar(this)
                // Las pantallas sin migrar todavía leen de acá.
                getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    .edit().clear().apply()

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