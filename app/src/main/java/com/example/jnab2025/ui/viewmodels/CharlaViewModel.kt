package com.example.jnab2025.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jnab2025.data.CharlaRepository
import com.example.jnab2025.models.Charla

class CharlaViewModel : ViewModel() {

    private val repository = CharlaRepository

    // LiveData para la lista de charlas
    private val _charlas = MutableLiveData<List<Charla>>()
    val charlas: LiveData<List<Charla>> = _charlas

    // LiveData para el estado del filtro
    private val _mostrandoSoloFavoritos = MutableLiveData<Boolean>(false)
    val mostrandoSoloFavoritos: LiveData<Boolean> = _mostrandoSoloFavoritos

    // LiveData para mensajes de estado
    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> = _mensaje

    // LiveData para charla seleccionada (por si necesitas detalle)
    private val _charlaSeleccionada = MutableLiveData<Charla?>()
    val charlaSeleccionada: LiveData<Charla?> = _charlaSeleccionada

    init {
        cargarTodasLasCharlas()
    }

    fun cargarTodasLasCharlas() {
        val todasLasCharlas = repository.obtenerTodasLasCharlas()
        _charlas.value = todasLasCharlas.sortedBy { it.fecha }
    }

    fun cargarCharlasFavoritas() {
        val charlasFavoritas = repository.obtenerCharlasFavoritas()
        _charlas.value = charlasFavoritas.sortedBy { it.fecha }
    }

    fun cargarCharlasDestacadas() {
        val charlasDestacadas = repository.obtenerCharlasDestacadas()
        _charlas.value = charlasDestacadas.sortedBy { it.fecha }
    }

    fun toggleFavorito(charla: Charla) {
        val esFavorito = repository.toggleFavorito(charla.id)

        // Actualizar mensaje
        _mensaje.value = if (esFavorito) {
            "Charla agregada a favoritos"
        } else {
            "Charla eliminada de favoritos"
        }

        // Recargar lista según el filtro actual
        actualizarListaSegunFiltro()
    }

    fun toggleFiltroFavoritos() {
        val nuevoEstado = !(_mostrandoSoloFavoritos.value ?: false)
        _mostrandoSoloFavoritos.value = nuevoEstado

        // Actualizar mensaje
        _mensaje.value = if (nuevoEstado) {
            "Mostrando solo favoritos"
        } else {
            "Mostrando todas las charlas"
        }

        // Actualizar la lista
        actualizarListaSegunFiltro()
    }

    private fun actualizarListaSegunFiltro() {
        val charlasList = if (_mostrandoSoloFavoritos.value == true) {
            repository.obtenerCharlasFavoritas()
        } else {
            repository.obtenerTodasLasCharlas()
        }
        _charlas.value = charlasList.sortedBy { it.fecha }
    }

    fun buscarCharlas(query: String) {
        if (query.isEmpty()) {
            actualizarListaSegunFiltro()
            return
        }

        val resultadosTitulo = repository.buscarCharlasPorTitulo(query)
        val resultadosExpositor = repository.buscarCharlasPorExpositor(query)

        // Combinar resultados sin duplicados
        val resultadosCombinados = (resultadosTitulo + resultadosExpositor).distinctBy { it.id }

        _charlas.value = resultadosCombinados.sortedBy { it.fecha }
    }

    fun filtrarPorFecha(fecha: String) {
        val charlasPorFecha = repository.obtenerCharlasPorFecha(fecha)
        _charlas.value = charlasPorFecha.sortedBy { it.horaInicio }
    }

    fun filtrarPorSala(sala: String) {
        val charlasPorSala = repository.buscarCharlasPorSala(sala)
        _charlas.value = charlasPorSala.sortedBy { it.fecha }
    }

    fun seleccionarCharla(charla: Charla) {
        _charlaSeleccionada.value = charla
    }

    fun limpiarSeleccion() {
        _charlaSeleccionada.value = null
    }

    fun limpiarMensaje() {
        _mensaje.value = ""
    }

    // Método para obtener todas las fechas disponibles
    fun obtenerFechasDisponibles(): List<String> {
        return repository.obtenerTodasLasCharlas()
            .map { it.fecha }
            .distinct()
            .sorted()
    }

    // Método para obtener todas las salas disponibles
    fun obtenerSalasDisponibles(): List<String> {
        return repository.obtenerTodasLasCharlas()
            .map { it.sala }
            .distinct()
            .sorted()
    }

    // Método para obtener una charla específica por ID
    fun obtenerCharlaPorId(id: Int): Charla? {
        return repository.obtenerCharlaPorId(id)
    }
}