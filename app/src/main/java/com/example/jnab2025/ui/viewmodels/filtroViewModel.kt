package com.example.jnab2025.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jnab2025.models.Categoria

class FiltroViewModel : ViewModel() {

    private val _filtrosSeleccionados = MutableLiveData<Set<Categoria>>(emptySet())
    val filtrosSeleccionados: LiveData<Set<Categoria>> = _filtrosSeleccionados

    fun toggleFiltro(categoria: Categoria) {
        val filtrosActuales = _filtrosSeleccionados.value ?: emptySet()
        _filtrosSeleccionados.value =
            if (filtrosActuales.contains(categoria)) {
                filtrosActuales - categoria
            } else {
                filtrosActuales + categoria
            }
    }

    fun limpiarFiltros() {
        _filtrosSeleccionados.value = emptySet()
    }
}

