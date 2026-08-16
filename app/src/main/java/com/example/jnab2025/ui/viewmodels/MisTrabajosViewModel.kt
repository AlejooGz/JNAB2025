package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.TrabajoConEstado
import com.example.jnab2025.data.repository.TrabajoRepository
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class MisTrabajosViewModel(application: Application) : AndroidViewModel(application) {

    private val db = JnabDatabase.get(application)
    private val repo = TrabajoRepository(db.trabajoDao(), db.simposioDao())

    private val usuarioId = Sesion.usuarioId(application)

    val haySesion = usuarioId != Sesion.SIN_SESION

    val trabajos: StateFlow<List<TrabajoConEstado>> =
        (if (haySesion) repo.seguimiento(usuarioId) else flowOf(emptyList()))
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
