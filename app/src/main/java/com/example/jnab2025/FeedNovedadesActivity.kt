package com.example.jnab2025

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.ActivityFeedNovedadesBinding

class FeedNovedadesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFeedNovedadesBinding
    private lateinit var novedadesAdapter: NovedadesAdapter

    //lista mutable
    private val novedadesList = mutableListOf<Novedad>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedNovedadesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configurar RecyclerView
        binding.recyclerViewNovedades.layoutManager = LinearLayoutManager(this)
        novedadesAdapter = NovedadesAdapter(novedadesList)
        binding.recyclerViewNovedades.adapter = novedadesAdapter

        // Cargar datos iniciales
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        novedadesList.apply {
            add(Novedad(titulo = "Cambio de aula", descripcion = "El Simposio 3 se pasa al aula 27", fecha = "27/04/2025", R.drawable.aula))
            add(Novedad(titulo = "Inicio de inscripciones", descripcion = "Desde hoy se abren las inscripciones a expositores", fecha = "20/04/2025", R.drawable.inscripciones))
            add(Novedad(titulo = "Coffee Break", descripcion = "Habrá coffee break a las 16:00hs en el pasillo fuera del laboratorio 1", fecha = "28/04/2025", R.drawable.breakcoffee))
        }
        novedadesAdapter.notifyDataSetChanged()
    }

    // función para agregar una nueva novedad dinamicamente
    private fun agregarNuevaNovedad(novedad: Novedad) {
        novedadesList.add(novedad)
        novedadesAdapter.notifyItemInserted(novedadesList.size - 1)
    }
}

