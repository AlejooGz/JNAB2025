package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.Usuario
import com.example.jnab2025.data.repository.OrganizadorRepository
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class InscriptosViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = OrganizadorRepository(JnabDatabase.get(application))
    private val eventoId = Sesion.eventoId(application)

    /** Los inscriptos de verdad, no una lista escrita a mano en el fragment. */
    val inscriptos: StateFlow<List<Usuario>> = repo.inscriptos(eventoId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
