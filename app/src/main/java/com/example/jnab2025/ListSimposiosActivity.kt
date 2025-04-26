package com.example.jnab2025

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListSimposiosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimposioAdapter

    // Lista mutable para poder actualizar favoritos
    private val listaSimposios = mutableListOf<Simposio>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_list_simposios)

        // Inicializar vistas
        recyclerView = findViewById(R.id.rvEventos)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar datos de ejemplo
        cargarDatosEjemplo()

        // Configurar adaptador
        adapter = SimposioAdapter(listaSimposios)
        recyclerView.adapter = adapter

    }

    private fun cargarDatosEjemplo() {
        // Datos de ejemplo - Estos serían reemplazados por datos reales de la API o base de datos
        listaSimposios.apply {
            clear()
            add(Simposio(
                id = 1,
                titulo = "Raíces de la Humanidad: Avances en Evolución y Diversidad5",
                fechaInicio = "2025-05-15",
                fechaFin = "2025-05-15"
            ))
            add(Simposio(
                id = 1,
                titulo = "Huella Genética: Nuevas Perspectivas en Evolución Humana",
                fechaInicio = "2025-05-16",
                fechaFin = "2025-05-16"
            ))
            add(Simposio(
                id = 1,
                titulo = "Genes, Cultura y Ambiente: Intersecciones Biológicas del Ser Humano",
                fechaInicio = "2025-05-15",
                fechaFin = "2025-05-16"
            ))
            add(Simposio(
                id = 1,
                titulo = "Huesos y Memoria: Lecturas Biológicas del Registro Arqueológico",
                fechaInicio = "2025-05-16",
                fechaFin = "2025-05-17"
            ))
            add(Simposio(
                id = 1,
                titulo = "Tecnologías Emergentes para el Estudio Biológico del Ser Humano",
                fechaInicio = "2025-05-17",
                fechaFin = "2025-05-17"
            ))
        }
    }
}