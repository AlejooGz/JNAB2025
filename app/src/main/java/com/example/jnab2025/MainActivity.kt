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
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import com.example.jnab2025.databinding.ActivityMainBinding
import com.example.jnab2025.ui.fragments.LoginFragment
import com.google.android.material.navigation.NavigationView

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

        // Mostrar el nombre del usuario logueado en el header
        val headerView = binding.navView.getHeaderView(0)
        val usernameTextView = headerView.findViewById<TextView>(R.id.tvDrawerUsername)

        val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isLoggedIn = !sharedPref.getString("username", null).isNullOrEmpty()
        val username = sharedPref.getString("username", null)
        Log.d("DEBUG", "Username: $username")
        Log.d("DEBUG", "isLoggedIn: $isLoggedIn")

        // if (isLoggedIn) {
        //     val headerView = binding.navView.getHeaderView(0)
        //     val usernameTextView = headerView.findViewById<TextView>(R.id.tvDrawerUsername)
        //     usernameTextView.text = username ?: "Invitado"
        // }

        binding.navHostFragment.post {
            val navController = findNavController(R.id.nav_host_fragment)
            navController.addOnDestinationChangedListener { _, destination, _ ->
                when (destination.id) {
                    R.id.loginFragment -> {
                        binding.toolbar.visibility = View.GONE
                        binding.navView.visibility = View.GONE
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                    }
                    else -> {
                        binding.toolbar.visibility = View.VISIBLE
                        binding.navView.visibility = View.VISIBLE
                        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                    }
                }
            }
        }
        // if (isLoggedIn) {
        //     // Redirigir al Login si no está logueado
        //     startActivity(Intent(this, LoginFragment::class.java))
        //     finish()
        //     return
        // }

        // val navigationView = binding.navView // o BottomNavigationView
        // val menu = navigationView.menu
        // menu.findItem(R.id.nav_perfil).isVisible = isLoggedIn

        // val toolBar = binding.toolbar // o BottomNavigationView
        // toolBar.isVisible = isLoggedIn

        val user = sharedPref.getString("username", "Invitado")
        usernameTextView.text = user
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_perfil -> {
                findNavController(R.id.nav_host_fragment)
                    .navigate(R.id.action_mainFragment_to_perfilFragment)
            }

            R.id.nav_logout -> {
                val sharedPref = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                sharedPref.edit().clear().apply()
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

                // Volver al login
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
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
