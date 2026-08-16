package com.example.jnab2025.ui.fragments

import androidx.navigation.fragment.navArgs

/**
 * Antes era una maqueta: mostraba "Simposio ejemplo N" y el guardado estaba
 * comentado. Ahora carga el simposio real y persiste los cambios.
 */
class EditarSimposioFragment : SimposioFormFragment() {

    private val args: EditarSimposioFragmentArgs by navArgs()

    override val simposioId get() = args.simposioId
    override val encabezado = "Editar simposio"
}
