package com.example.jnab2025

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AgendaActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CharlaAdapter
    private lateinit var fabFiltrar: FloatingActionButton

    // Lista mutable para poder actualizar favoritos
    private val listaCharlas = mutableListOf<Charla>()

    // Variable para controlar si estamos mostrando solo favoritos o todos
    private var mostrandoSoloFavoritos = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agenda)

        // Inicializar vistas
        recyclerView = findViewById(R.id.rvEventos)
        fabFiltrar = findViewById(R.id.fabFiltrar)

        // Configurar RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Cargar datos de ejemplo
        cargarDatosEjemplo()

        // Configurar adaptador
        adapter = CharlaAdapter(listaCharlas.sortedBy { it.fecha }) { charla ->
            // Callback para cuando se hace clic en el botón de favorito
            toggleFavorito(charla)
        }
        recyclerView.adapter = adapter

        // Configurar botón de filtro
        fabFiltrar.setOnClickListener {
            toggleFiltroFavoritos()
        }
    }

    private fun cargarDatosEjemplo() {
        // Datos de ejemplo - Estos serían reemplazados por datos reales de la API o base de datos
        listaCharlas.apply {
            clear()
            add(Charla(
                id = 1,
                titulo = "Conferencia Inaugural JNAB 2025",
                expositor = "Dr. Roberto García - Comité Organizador",
                fecha = "2025-05-15",
                horaInicio = "09:00",
                horaFin = "09:30",
                sala = "Auditorio Principal",
                esFavorito = false,
                esDestacado = true
            ))
            add(Charla(
                id = 2,
                titulo = "Innovaciones en IA para el Desarrollo de Software",
                expositor = "Dra. María Sánchez - Universidad Tecnológica",
                fecha = "2025-05-15",
                horaInicio = "14:00",
                horaFin = "14:30",
                sala = "Sala 3",
                esFavorito = true,
                esDestacado = true
            ))
            add(Charla(
                id = 3,
                titulo = "Frameworks para Desarrollo Móvil",
                expositor = "Ing. Carlos López - Tech Mobile Inc.",
                fecha = "2025-05-16",
                horaInicio = "10:30",
                horaFin = "11:00",
                sala = "Sala 2",
                esFavorito = false,
                esDestacado = false
            ))
            add(Charla(
                id = 4,
                titulo = "Networking: Conectando Profesionales",
                expositor = "Lic. Ana Martínez - Consultora de RRHH",
                fecha = "2025-05-16",
                horaInicio = "16:00",
                horaFin = "16:30",
                sala = "Hall Central",
                esFavorito = true,
                esDestacado = true
            ))
            add(Charla(
                id = 5,
                titulo = "Nuevas Tecnologías en la Nube",
                expositor = "Ing. Pedro Ramírez - CloudTech (Sponsor Principal)",
                fecha = "2025-05-17",
                horaInicio = "11:00",
                horaFin = "11:30",
                sala = "Auditorio Principal",
                esFavorito = false,
                esDestacado = true
            ))
            add(Charla(
                id = 6,
                titulo = "Panel: Tendencias en Ciberseguridad",
                expositor = "Panel de Expertos - Moderador: Dr. Javier Díaz",
                fecha = "2025-05-17",
                horaInicio = "14:30",
                horaFin = "15:00",
                sala = "Sala 4",
                esFavorito = false,
                esDestacado = false
            ))
            add(Charla(
                id = 7,
                titulo = "Ceremonia de Clausura JNAB 2025",
                expositor = "Comité Organizador",
                fecha = "2025-05-17",
                horaInicio = "18:00",
                horaFin = "18:30",
                sala = "Auditorio Principal",
                esFavorito = true,
                esDestacado = true
            ))
        }
    }

    private fun toggleFavorito(charla: Charla) {
        // Buscar la charla en la lista original y cambiar su estado
        val charlaEnLista = listaCharlas.find { it.id == charla.id }
        charlaEnLista?.let {
            it.esFavorito = !it.esFavorito

            // Mostrar mensaje
            val mensaje = if (it.esFavorito)
                "Charla agregada a favoritos"
            else
                "Charla eliminada de favoritos"
            Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

            // Actualizar la lista según el filtro actual
            actualizarListaCharlas()
        }
    }

    private fun toggleFiltroFavoritos() {
        mostrandoSoloFavoritos = !mostrandoSoloFavoritos

        // Mostrar mensaje del estado actual
        val mensaje = if (mostrandoSoloFavoritos)
            "Mostrando solo favoritos"
        else
            "Mostrando todas las charlas"
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

        // Actualizar lista
        actualizarListaCharlas()
    }

    private fun actualizarListaCharlas() {
        val listaFiltrada = if (mostrandoSoloFavoritos) {
            listaCharlas.filter { it.esFavorito }
        } else {
            listaCharlas
        }

        // Ordenar por fecha
        adapter.actualizarCharlas(listaFiltrada.sortedBy { it.fecha })
    }
}