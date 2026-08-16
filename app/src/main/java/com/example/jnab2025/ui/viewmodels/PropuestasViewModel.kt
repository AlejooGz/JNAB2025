package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.PropuestaPendiente
import com.example.jnab2025.data.model.Simposio
import com.example.jnab2025.data.model.Trabajo
import com.example.jnab2025.data.repository.Aprobacion
import com.example.jnab2025.data.repository.OrganizadorRepository
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

class PropuestasViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = OrganizadorRepository(JnabDatabase.get(application))
    private val usuarioId = Sesion.usuarioId(application)

    private val _simposioId = MutableStateFlow(0L)

    private val _simposio = MutableStateFlow<Simposio?>(null)
    val simposio: StateFlow<Simposio?> = _simposio.asStateFlow()

    private val _trabajo = MutableStateFlow<Trabajo?>(null)
    val trabajo: StateFlow<Trabajo?> = _trabajo.asStateFlow()

    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()

    private val _resueltas = Channel<Unit>(Channel.BUFFERED)
    val resueltas: Flow<Unit> = _resueltas.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val propuestas: StateFlow<List<PropuestaPendiente>> = _simposioId
        .flatMapLatest { id -> repo.propuestasPendientes(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun cargarSimposio(simposioId: Long) {
        _simposioId.value = simposioId
        viewModelScope.launch { _simposio.value = repo.simposio(simposioId) }
    }

    /** Para las pantallas de aceptar y rechazar, que reciben el trabajo. */
    fun cargarTrabajo(trabajoId: Long) = viewModelScope.launch {
        val trabajo = repo.trabajo(trabajoId)
        _trabajo.value = trabajo
        _simposio.value = trabajo?.let { repo.simposio(it.simposioId) }
    }

    fun aprobar(trabajoId: Long, fecha: LocalDate, horaInicio: LocalTime) = viewModelScope.launch {
        val resultado = runCatching { repo.aprobar(trabajoId, usuarioId, fecha, horaInicio) }
            .getOrElse {
                _avisos.send("No se pudo programar: ${it.message}")
                return@launch
            }

        when (resultado) {
            is Aprobacion.Ok -> {
                _avisos.send("Programada el ${resultado.fecha} de ${resultado.desde} a ${resultado.hasta}")
                _resueltas.send(Unit)
            }

            is Aprobacion.FueraDelSimposio ->
                _avisos.send("El simposio va del ${resultado.desde} al ${resultado.hasta}")

            is Aprobacion.HorarioOcupado ->
                _avisos.send(
                    "Ese horario ya lo ocupa \"${resultado.titulo}\" " +
                        "(${resultado.desde}-${resultado.hasta})"
                )

            Aprobacion.NoExiste -> _avisos.send("No se encontro el trabajo")
        }
    }

    fun rechazar(trabajoId: Long, motivo: String) = viewModelScope.launch {
        if (motivo.isBlank()) {
            _avisos.send("Escribi el motivo del rechazo")
            return@launch
        }
        repo.rechazar(trabajoId, usuarioId, motivo.trim())
        _avisos.send("Propuesta rechazada")
        _resueltas.send(Unit)
    }
}
