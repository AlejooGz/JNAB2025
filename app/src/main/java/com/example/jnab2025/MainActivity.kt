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
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.jnab2025.databinding.ActivityMainBinding
import com.example.jnab2025.utils.Sesion
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

    // El seed de aulas ya no corre aca: se dispara desde LoginFragment, cuando
    // hay sesion. Hacerlo en onCreate lo ejecutaba sin usuario autenticado, y
    // ademas pisaba las aulas en cada arranque.
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
                Sesion.cerrar(this)
                FirebaseAuth.getInstance().signOut()
                getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                    .edit().clear().apply()
                findNavController(R.id.nav_host_fragment).navigate(
                    R.id.loginFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build()
                )
            }
            R.id.nav_crear_novedad -> {
                findNavController(R.id.nav_host_fragment
                ).navigate(R.id.crearNovedadFragment
                )
            }
            R.id.nav_gestionar_faq -> {
                findNavController(R.id.nav_host_fragment
                ).navigate(R.id.gestionFaqFragment
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