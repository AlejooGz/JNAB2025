package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.ItemAgenda
import com.example.jnab2025.data.repository.CronogramaRepository
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class CronogramaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JnabDatabase.get(application)
    private val repo = CronogramaRepository(db.charlaDao(), db.agendaDao())

    private val usuarioId = Sesion.usuarioId(application)
    private val eventoId = Sesion.eventoId(application)

    private val _dia = MutableStateFlow<LocalDate?>(null)
    val dia: StateFlow<LocalDate?> = _dia.asStateFlow()

    private val _soloMiAgenda = MutableStateFlow(false)
    val soloMiAgenda: StateFlow<Boolean> = _soloMiAgenda.asStateFlow()

    /** Avisos de una sola lectura: no se repiten al rotar la pantalla. */
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()

    val dias: StateFlow<List<LocalDate>> = repo.dias(eventoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<ItemAgenda>> =
        combine(_dia, _soloMiAgenda) { dia, solo -> dia to solo }
            .flatMapLatest { (dia, solo) ->
                if (dia == null) {
                    flowOf(emptyList())
                } else {
                    repo.cronogramaDelDia(eventoId, dia, usuarioId)
                        .map { lista -> if (solo) lista.filter { it.enMiAgenda } else lista }
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            // Arranca en el primer dia del congreso que tenga actividades.
            _dia.value = repo.dias(eventoId).first().firstOrNull()
        }
    }

    fun seleccionarDia(dia: LocalDate) {
        _dia.value = dia
    }

    fun alternarSoloMiAgenda() {
        _soloMiAgenda.value = !_soloMiAgenda.value
    }

    fun alternarAgenda(item: ItemAgenda) = viewModelScope.launch {
        if (usuarioId == Sesion.SIN_SESION) {
            _avisos.send("Inicia sesion para armar tu agenda")
            return@launch
        }

        if (item.enMiAgenda) {
            repo.quitarDeAgenda(usuarioId, item.charlaId)
            _avisos.send("Quitada de tu agenda")
            return@launch
        }

        val choques = repo.agregarAAgenda(usuarioId, item)
        _avisos.send(
            if (choques.isEmpty()) {
                "Agregada a tu agenda"
            } else {
                val otra = choques.first()
                "Agregada, pero se superpone con \"${otra.titulo}\" (${otra.horaInicio}-${otra.horaFin})"
            }
        )
    }
}
