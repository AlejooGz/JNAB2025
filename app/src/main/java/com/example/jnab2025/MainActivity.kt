package com.example.jnab2025

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.jnab2025.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.btnShare.setOnClickListener {
            val message = binding.editTextMessage.text.toString().trim()

            if (message.isEmpty()) {
                Toast.makeText(this, "Por favor, escribe un mensaje para compartir", Toast.LENGTH_SHORT).show()
            } else {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, message)
                    type = "text/plain"
                }

                val chooser = Intent.createChooser(sendIntent, "Compartir con...")
                startActivity(chooser)
            }
        }

        binding.btnListSimposios.setOnClickListener {
            val intent = Intent(this, ListSimposiosActivity::class.java)
            startActivity(intent)
        }

        binding.btnVerAgenda.setOnClickListener {
            val intent = Intent(this, AgendaActivity::class.java)
            startActivity(intent)
        }

        binding.btnFeedNovedades.setOnClickListener {
            val intent = Intent(this, FeedNovedadesActivity::class.java)
            startActivity(intent)
        }

    }
}