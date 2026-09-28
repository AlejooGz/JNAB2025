package com.example.jnab2025.ui.fragments

import androidx.navigation.fragment.navArgs

class EditarActividadFragment :
    ActividadFormFragment() {
    private val args:
            EditarActividadFragmentArgs by navArgs()
    override val actividadId: String
        get() = args.actividadId
    override val encabezado: String
        get() = "Editar actividad"
}