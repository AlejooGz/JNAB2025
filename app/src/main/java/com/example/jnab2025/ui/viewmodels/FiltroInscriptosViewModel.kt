package com.example.jnab2025.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.jnab2025.data.model.CategoriaInscripcion
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.TipoInscripcion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FiltroInscriptosViewModel : ViewModel() {
    private val _estadosSeleccionados =
        MutableStateFlow<Set<EstadoComprobante>>(emptySet())
    val estadosSeleccionados: StateFlow<Set<EstadoComprobante>> =
        _estadosSeleccionados.asStateFlow()
    private val _tiposSeleccionados =
        MutableStateFlow<Set<TipoInscripcion>>(emptySet())
    val tiposSeleccionados: StateFlow<Set<TipoInscripcion>> =
        _tiposSeleccionados.asStateFlow()
    private val _categoriasSeleccionadas =
        MutableStateFlow<Set<CategoriaInscripcion>>(emptySet())
    val categoriasSeleccionadas: StateFlow<Set<CategoriaInscripcion>> =
        _categoriasSeleccionadas.asStateFlow()
    fun toggleEstado(estado: EstadoComprobante) {
        val actuales = _estadosSeleccionados.value
        _estadosSeleccionados.value =
            if (estado in actuales) {
                actuales - estado
            } else {
                actuales + estado
            }
    }
    fun toggleTipo(tipo: TipoInscripcion) {
        val actuales = _tiposSeleccionados.value
        _tiposSeleccionados.value =
            if (tipo in actuales) {
                actuales - tipo
            } else {
                actuales + tipo
            }
    }
    fun toggleCategoria(categoria: CategoriaInscripcion) {
        val actuales = _categoriasSeleccionadas.value
        _categoriasSeleccionadas.value =
            if (categoria in actuales) {
                actuales - categoria
            } else {
                actuales + categoria
            }
    }
    fun limpiarFiltros() {
        _estadosSeleccionados.value = emptySet()
        _tiposSeleccionados.value = emptySet()
        _categoriasSeleccionadas.value = emptySet()
    }
}