package com.example.jnab2025.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jnab2025.data.model.CategoriaLugar

class FiltroViewModel : ViewModel() {
    private val _filtrosSeleccionados =
        MutableLiveData<Set<CategoriaLugar>>(
            emptySet()
        )
    val filtrosSeleccionados:
            LiveData<Set<CategoriaLugar>> = _filtrosSeleccionados
    fun toggleFiltro(
        categoria: CategoriaLugar
    ) {
        val filtrosActuales =
            _filtrosSeleccionados.value
                ?: emptySet()
        _filtrosSeleccionados.value =
            if (
                filtrosActuales.contains(categoria
                )
            ) {
                filtrosActuales - categoria

            } else {
                filtrosActuales + categoria
            }
    }
    fun limpiarFiltros() {
        _filtrosSeleccionados.value =
            emptySet()
    }
}