package com.example.jnab2025

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SimposiosFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimposioAdapter
    private val listaSimposios = mutableListOf<Simposio>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_simposio, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.rvEventos)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        cargarDatosEjemplo()
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
