package com.example.jnab2025.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.jnab2025.models.Categoria

class FiltroViewModel : ViewModel() {

    private val _filtroSeleccionado = MutableLiveData<Categoria?>()
    val filtroSeleccionado: LiveData<Categoria?> = _filtroSeleccionado

    fun aplicarFiltro(categoria: Categoria) {
        _filtroSeleccionado.value = categoria
    }

    fun limpiarFiltro() {
        _filtroSeleccionado.value = null
    }
}
