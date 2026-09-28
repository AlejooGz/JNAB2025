package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.ViewModelProvider
import com.example.jnab2025.R
import com.example.jnab2025.data.model.CategoriaInscripcion
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.TipoInscripcion
import com.example.jnab2025.ui.viewmodels.FiltroInscriptosViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class FiltroInscriptosBottomSheetFragment :
    BottomSheetDialogFragment() {
    private lateinit var filtroViewModel: FiltroInscriptosViewModel
    private lateinit var btnPendiente: MaterialButton
    private lateinit var btnVerificado: MaterialButton
    private lateinit var btnRechazado: MaterialButton
    private lateinit var btnAsistente: MaterialButton
    private lateinit var btnExpositor: MaterialButton
    private lateinit var btnGeneral: MaterialButton
    private lateinit var btnEstudiante: MaterialButton
    private lateinit var btnCerrar: ImageButton
    private lateinit var btnQuitarFiltros: MaterialButton
    private lateinit var grupoEstados: MaterialButtonToggleGroup
    private lateinit var grupoTipos: MaterialButtonToggleGroup
    private lateinit var grupoCategorias: MaterialButtonToggleGroup


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(
            R.layout.fragment_filtro_inscriptos_bottom_sheet,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )
        filtroViewModel =
            ViewModelProvider(requireActivity())[
                FiltroInscriptosViewModel::class.java
            ]

        btnPendiente = view.findViewById(R.id.btnPendiente)
        btnVerificado = view.findViewById(R.id.btnVerificado)
        btnRechazado = view.findViewById(R.id.btnRechazado)
        btnAsistente = view.findViewById(R.id.btnAsistente)
        btnExpositor = view.findViewById(R.id.btnExpositor)
        btnGeneral = view.findViewById(R.id.btnGeneral)
        btnEstudiante = view.findViewById(R.id.btnEstudiante)
        btnCerrar = view.findViewById(R.id.btnCerrar)
        btnQuitarFiltros = view.findViewById(R.id.btnQuitarFiltros)
        grupoEstados = view.findViewById(R.id.grupoEstados)
        grupoTipos = view.findViewById(R.id.grupoTipos)
        grupoCategorias = view.findViewById(R.id.grupoCategorias)

        configurarSeleccionActual()
        configurarListeners()

        btnCerrar.setOnClickListener { dismiss()
        }
        btnQuitarFiltros.setOnClickListener {
            filtroViewModel.limpiarFiltros()
            grupoEstados.clearChecked()
            grupoTipos.clearChecked()
            grupoCategorias.clearChecked()
        }
    }

    private fun configurarSeleccionActual() {
        val estados = filtroViewModel.estadosSeleccionados.value
        val tipos = filtroViewModel.tiposSeleccionados.value
        val categorias = filtroViewModel.categoriasSeleccionadas.value

        btnPendiente.isChecked = EstadoComprobante.PENDIENTE in estados
        btnVerificado.isChecked = EstadoComprobante.VERIFICADO in estados
        btnRechazado.isChecked = EstadoComprobante.RECHAZADO in estados
        btnAsistente.isChecked = TipoInscripcion.ASISTENTE in tipos
        btnExpositor.isChecked = TipoInscripcion.EXPOSITOR in tipos
        btnGeneral.isChecked = CategoriaInscripcion.GENERAL in categorias
        btnEstudiante.isChecked = CategoriaInscripcion.ESTUDIANTE in categorias
    }
    private fun configurarListeners() {
        btnPendiente.setOnClickListener {
            filtroViewModel.toggleEstado(
                EstadoComprobante.PENDIENTE
            )
        }
        btnVerificado.setOnClickListener {
            filtroViewModel.toggleEstado(
                EstadoComprobante.VERIFICADO
            )
        }
        btnRechazado.setOnClickListener {
            filtroViewModel.toggleEstado(
                EstadoComprobante.RECHAZADO
            )
        }
        btnAsistente.setOnClickListener {
            filtroViewModel.toggleTipo(
                TipoInscripcion.ASISTENTE
            )
        }
        btnExpositor.setOnClickListener {
            filtroViewModel.toggleTipo(
                TipoInscripcion.EXPOSITOR
            )
        }
        btnGeneral.setOnClickListener {
            filtroViewModel.toggleCategoria(
                CategoriaInscripcion.GENERAL
            )
        }
        btnEstudiante.setOnClickListener {
            filtroViewModel.toggleCategoria(
                CategoriaInscripcion.ESTUDIANTE
            )
        }
    }
}