package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.SimposioConAula
import com.example.jnab2025.data.repository.OrganizadorRepository
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MisSimposiosViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = OrganizadorRepository(JnabDatabase.get(application))

    private val usuarioId = Sesion.usuarioId(application)
    private val eventoId = Sesion.eventoId(application)

    /** Solo los simposios que organiza el usuario logueado, no todos. */
    val simposios: StateFlow<List<SimposioConAula>> = repo.misSimposios(eventoId, usuarioId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
