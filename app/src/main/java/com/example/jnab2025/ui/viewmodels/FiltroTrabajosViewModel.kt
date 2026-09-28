package com.example.jnab2025.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.jnab2025.data.model.EstadoTrabajo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class FiltroTrabajosViewModel : ViewModel() {

    private val _estadosSeleccionados =
        MutableStateFlow<Set<EstadoTrabajo>>(emptySet())

    val estadosSeleccionados: StateFlow<Set<EstadoTrabajo>> =
        _estadosSeleccionados.asStateFlow()

    private val _programacionSeleccionada =
        MutableStateFlow<Set<FiltroProgramacion>>(emptySet())

    val programacionSeleccionada:
            StateFlow<Set<FiltroProgramacion>> =
        _programacionSeleccionada.asStateFlow()

    fun alternarEstado(
        estado: EstadoTrabajo
    ) {
        _estadosSeleccionados.update { actuales ->

            if (estado in actuales) {
                actuales - estado
            } else {
                actuales + estado
            }
        }
    }

    fun alternarProgramacion(
        filtro: FiltroProgramacion
    ) {
        _programacionSeleccionada.update { actuales ->

            if (filtro in actuales) {
                actuales - filtro
            } else {
                actuales + filtro
            }
        }
    }

    fun limpiar() {
        _estadosSeleccionados.value =
            emptySet()

        _programacionSeleccionada.value =
            emptySet()
    }

    enum class FiltroProgramacion {
        PROGRAMADO,
        SIN_PROGRAMAR
    }
}