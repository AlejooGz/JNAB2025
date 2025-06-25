package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.jnab2025.data.CharlaFakeData
import com.example.jnab2025.data.UserFakeData
import com.example.jnab2025.data.db.AppDatabase
import com.example.jnab2025.data.repository.CharlaRepository
import com.example.jnab2025.models.Charla
import com.example.jnab2025.models.EstadoPropuesta
import com.example.jnab2025.models.User
import kotlinx.coroutines.launch

class CharlaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CharlaRepository
    private val _charlas = MutableLiveData<List<Charla>>()
    val charlas: LiveData<List<Charla>> get() = _charlas

    private val _mensaje = MutableLiveData<String>()
    val mensaje: LiveData<String> get() = _mensaje

    // Para el filtro de favoritos
    private val _mostrandoSoloFavoritos = MutableLiveData(false)
    val mostrandoSoloFavoritos: LiveData<Boolean> get() = _mostrandoSoloFavoritos

    init {
        val dao = AppDatabase.getDatabase(application).charlaDao()
        repository = CharlaRepository(dao)
        cargarTodasLasCharlas()
    }

    fun cargarTodasLasCharlas() = viewModelScope.launch {
        val lista = repository.obtenerTodas()
        _charlas.value = if (_mostrandoSoloFavoritos.value == true) {
            lista.filter { it.esFavorito }
        } else {
            lista
        }
    }

    fun cargarCharlasPendientesPorSimposio(simposioId: Int) = viewModelScope.launch {
        val todas = repository.obtenerPorSimposio(simposioId)
        val filtradas = todas.filter { it.estado == EstadoPropuesta.PENDIENTE }
        _charlas.value = filtrarPorFavoritosSiCorresponde(filtradas)
    }

    fun cargarCharlasPorSimposio(simposioId: Int) = viewModelScope.launch {
        val lista = repository.obtenerPorSimposio(simposioId)
        _charlas.value = filtrarPorFavoritosSiCorresponde(lista)
    }

    fun cargarCharlasPorExpositor(expositorId: Int) = viewModelScope.launch {
        val lista = repository.obtenerPorExpositor(expositorId)
        _charlas.value = filtrarPorFavoritosSiCorresponde(lista)
    }

    fun cargarCharlasPorEstado(estado: EstadoPropuesta) = viewModelScope.launch {
        val lista = repository.obtenerPorEstado(estado)
        _charlas.value = filtrarPorFavoritosSiCorresponde(lista)
    }

    private fun filtrarPorFavoritosSiCorresponde(lista: List<Charla>): List<Charla> {
        return if (_mostrandoSoloFavoritos.value == true) {
            lista.filter { it.esFavorito }
        } else {
            lista
        }
    }

    fun crearCharla(charla: Charla) = viewModelScope.launch {
        repository.crearCharla(charla)
        _mensaje.value = "Charla enviada correctamente"
        cargarTodasLasCharlas()
    }

    fun insertarTodos(lista: List<Charla>) = viewModelScope.launch {
        repository.insertarTodos(lista)
    }

    fun editarCharla(charlaOriginal: Charla, nuevoTitulo: String, nuevaDescripcion: String?, nuevoNombreArchivo: String) = viewModelScope.launch {
        val charlaEditada = charlaOriginal.copy(
            titulo = nuevoTitulo,
            descripcion = nuevaDescripcion,
            nombreArchivo = nuevoNombreArchivo
        )

        repository.actualizarCharla(charlaEditada)
        _mensaje.value = "Charla actualizada correctamente"
        cargarTodasLasCharlas()
    }

    fun eliminar(charla: Charla) = viewModelScope.launch {
        repository.eliminar(charla)
    }

    fun eliminarTodos() = viewModelScope.launch {
        repository.eliminarTodos()
    }



    fun aprobarCharla(charla: Charla, fecha: String, inicio: String, fin: String, sala: String) = viewModelScope.launch {
        val aprobada = charla.copy(
            estado = EstadoPropuesta.APROBADA,
            fechaExposicion = fecha,
            horaInicio = inicio,
            horaFin = fin,
            sala = sala
        )
        repository.actualizarCharla(aprobada)
        _mensaje.value = "Charla aprobada"
        cargarTodasLasCharlas()
    }

    fun rechazarCharla(charla: Charla) = viewModelScope.launch {
        val rechazada = charla.copy(estado = EstadoPropuesta.RECHAZADA)
        repository.actualizarCharla(rechazada)
        _mensaje.value = "Charla rechazada"
        cargarTodasLasCharlas()
    }

    fun marcarComoPagada(charla: Charla) = viewModelScope.launch {
        val pagada = charla.copy(pagado = true)
        repository.actualizarCharla(pagada)
        _mensaje.value = "Charla pagada correctamente"
        cargarTodasLasCharlas()
    }

    fun toggleFavorito(charla: Charla) = viewModelScope.launch {
        val charlaActualizada = charla.copy(esFavorito = !charla.esFavorito)
        repository.actualizarCharla(charlaActualizada)
        cargarTodasLasCharlas()
    }

    fun toggleFiltroFavoritos() {
        _mostrandoSoloFavoritos.value = !_mostrandoSoloFavoritos.value!!
        cargarTodasLasCharlas()
    }

    fun limpiarMensaje() {
        _mensaje.value = ""
    }
}
