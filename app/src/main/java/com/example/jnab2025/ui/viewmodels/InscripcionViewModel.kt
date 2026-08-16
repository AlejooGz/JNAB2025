package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.ComprobantePago
import com.example.jnab2025.data.model.Evento
import com.example.jnab2025.data.model.Inscripcion
import com.example.jnab2025.data.model.TipoInscripcion
import com.example.jnab2025.data.repository.InscripcionRepository
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InscripcionViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = InscripcionRepository(JnabDatabase.get(application))

    private val usuarioId = Sesion.usuarioId(application)
    private val eventoId = Sesion.eventoId(application)

    /** Todo lo que la pantalla necesita saber sobre mi inscripcion. */
    data class Vista(
        val evento: Evento? = null,
        val inscripcion: Inscripcion? = null,
        val comprobante: ComprobantePago? = null
    )

    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()

    private val inscripcionFlow = repo.inscripcion(usuarioId, eventoId)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val comprobanteFlow = inscripcionFlow.flatMapLatest { inscripcion ->
        if (inscripcion == null) flowOf(null) else repo.comprobante(inscripcion.id)
    }

    val vista: StateFlow<Vista> =
        combine(repo.evento(eventoId), inscripcionFlow, comprobanteFlow) { evento, inscripcion, comprobante ->
            Vista(evento, inscripcion, comprobante)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Vista())

    fun montoPara(tipo: TipoInscripcion): Double? =
        vista.value.evento?.let { repo.montoPara(tipo, it) }

    fun inscribirse(tipo: TipoInscripcion) = viewModelScope.launch {
        if (usuarioId == Sesion.SIN_SESION) {
            _avisos.send("Inicia sesion para inscribirte")
            return@launch
        }
        val evento = vista.value.evento ?: repo.evento(eventoId).first()
        if (evento == null) {
            _avisos.send("No hay ningun evento cargado")
            return@launch
        }
        runCatching { repo.inscribir(usuarioId, evento, tipo) }
            .onSuccess { _avisos.send("Inscripcion registrada. Ahora carga el comprobante de pago") }
            .onFailure { _avisos.send("No se pudo inscribir: ${it.message}") }
    }

    fun cargarComprobante(archivoUri: String?, nombreArchivo: String?) = viewModelScope.launch {
        val inscripcion = vista.value.inscripcion
        when {
            inscripcion == null -> _avisos.send("Primero inscribite al evento")
            archivoUri == null -> _avisos.send("Elegi el archivo del comprobante")
            else -> runCatching {
                repo.cargarComprobante(inscripcion.id, archivoUri, nombreArchivo ?: "comprobante")
            }.onSuccess {
                _avisos.send("Comprobante enviado. La organizacion lo va a verificar")
            }.onFailure {
                _avisos.send("No se pudo guardar el comprobante: ${it.message}")
            }
        }
    }
}
