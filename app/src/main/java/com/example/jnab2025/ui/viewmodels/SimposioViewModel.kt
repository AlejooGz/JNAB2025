package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.Simposio
import com.example.jnab2025.data.repository.SimposioRepository
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SimposioViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val database = JnabDatabase.get(application)

    private val repository =
        SimposioRepository(database.simposioDao())

    private val eventoId = Sesion.eventoId(application)

    val simposios: StateFlow<List<Simposio>> =
        repository.simposiosDelEvento(eventoId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    fun insertar(simposio: Simposio) {
        viewModelScope.launch {
            repository.insertar(simposio)
        }
    }

    fun actualizar(simposio: Simposio) {
        viewModelScope.launch {
            repository.actualizar(simposio)
        }
    }

    fun eliminar(simposio: Simposio) {
        viewModelScope.launch {
            repository.eliminar(simposio)
        }
    }
}