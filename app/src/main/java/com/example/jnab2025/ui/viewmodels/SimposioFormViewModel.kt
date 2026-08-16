package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.Aula
import com.example.jnab2025.data.model.Simposio
import com.example.jnab2025.data.repository.OrganizadorRepository
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Sirve para crear y para editar: la diferencia es si llega con un id o no. */
class SimposioFormViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = OrganizadorRepository(JnabDatabase.get(application))
    private val usuarioId = Sesion.usuarioId(application)
    private val eventoId = Sesion.eventoId(application)

    val aulas: StateFlow<List<Aula>> = repo.aulas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _simposio = MutableStateFlow<Simposio?>(null)
    val simposio: StateFlow<Simposio?> = _simposio.asStateFlow()

    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()

    private val _guardados = Channel<Unit>(Channel.BUFFERED)
    val guardados: Flow<Unit> = _guardados.receiveAsFlow()

    fun cargar(simposioId: Long) = viewModelScope.launch {
        _simposio.value = repo.simposio(simposioId)
    }

    fun guardar(
        simposioId: Long,
        titulo: String,
        tema: String,
        descripcion: String,
        aulaId: Long?,
        desde: LocalDate?,
        hasta: LocalDate?
    ) = viewModelScope.launch {
        when {
            titulo.isBlank() -> return@launch avisar("Falta el titulo")
            tema.isBlank() -> return@launch avisar("Falta el tema central")
            descripcion.isBlank() -> return@launch avisar("Falta la descripcion")
            aulaId == null -> return@launch avisar("Elegi un aula")
            desde == null || hasta == null -> return@launch avisar("Elegi las fechas")
            hasta < desde -> return@launch avisar("La fecha de fin es anterior a la de inicio")
        }

        // Como el aula es del simposio, alcanza con chequear acá para que dos
        // simposios no se pisen en la misma sala.
        val choques = repo.conflictosDeAula(aulaId!!, desde!!, hasta!!, simposioId)
        if (choques.isNotEmpty()) {
            return@launch avisar("Esa aula ya la ocupa \"${choques.first().titulo}\" en esas fechas")
        }

        val existente = _simposio.value
        runCatching {
            if (simposioId == 0L || existente == null) {
                repo.crearSimposio(
                    Simposio(
                        eventoId = eventoId,
                        organizadorId = usuarioId,
                        aulaId = aulaId,
                        titulo = titulo.trim(),
                        descripcion = descripcion.trim(),
                        temaCentral = tema.trim(),
                        fechaInicio = desde,
                        fechaFin = hasta
                    )
                )
            } else {
                repo.actualizarSimposio(
                    existente.copy(
                        aulaId = aulaId,
                        titulo = titulo.trim(),
                        descripcion = descripcion.trim(),
                        temaCentral = tema.trim(),
                        fechaInicio = desde,
                        fechaFin = hasta
                    )
                )
            }
        }.onSuccess {
            _avisos.send(if (simposioId == 0L) "Simposio creado" else "Cambios guardados")
            _guardados.send(Unit)
        }.onFailure {
            _avisos.send("No se pudo guardar: ${it.message}")
        }
    }

    private suspend fun avisar(mensaje: String) {
        _avisos.send(mensaje)
    }
}
