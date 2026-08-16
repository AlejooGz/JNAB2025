package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.SimposioConAula
import com.example.jnab2025.data.repository.TrabajoRepository
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EnviarTrabajoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JnabDatabase.get(application)
    private val repo = TrabajoRepository(db.trabajoDao(), db.simposioDao())

    private val usuarioId = Sesion.usuarioId(application)
    private val eventoId = Sesion.eventoId(application)

    sealed interface Envio {
        data object Ok : Envio
        data class Error(val mensaje: String) : Envio
    }

    private val _envios = Channel<Envio>(Channel.BUFFERED)
    val envios: Flow<Envio> = _envios.receiveAsFlow()

    val simposios: StateFlow<List<SimposioConAula>> = repo.simposiosDisponibles(eventoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun enviar(
        simposioId: Long,
        titulo: String,
        resumen: String,
        archivoUri: String?,
        nombreArchivo: String?
    ) = viewModelScope.launch {
        val error = validar(simposioId, titulo, resumen, archivoUri)
        if (error != null) {
            _envios.send(Envio.Error(error))
            return@launch
        }

        runCatching {
            repo.enviar(
                simposioId = simposioId,
                autorId = usuarioId,
                titulo = titulo.trim(),
                resumen = resumen.trim(),
                archivoUri = archivoUri!!,
                nombreArchivo = nombreArchivo ?: "trabajo.pdf"
            )
        }.onSuccess {
            _envios.send(Envio.Ok)
        }.onFailure {
            _envios.send(Envio.Error("No se pudo guardar el trabajo: ${it.message}"))
        }
    }

    private fun validar(
        simposioId: Long,
        titulo: String,
        resumen: String,
        archivoUri: String?
    ): String? = when {
        usuarioId == Sesion.SIN_SESION -> "Inicia sesion para enviar un trabajo"
        simposioId <= 0L -> "No se pudo identificar el simposio"
        titulo.isBlank() -> "Falta el titulo del trabajo"
        resumen.isBlank() -> "Falta el resumen"
        archivoUri == null -> "Falta adjuntar el PDF"
        else -> null
    }
}
