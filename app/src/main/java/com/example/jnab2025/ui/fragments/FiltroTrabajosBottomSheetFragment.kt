package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.databinding.BottomSheetFiltroTrabajosBinding
import com.example.jnab2025.ui.viewmodels.FiltroTrabajosViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton

class FiltroTrabajosBottomSheetFragment :
    BottomSheetDialogFragment() {

    private var _binding:
            BottomSheetFiltroTrabajosBinding? = null

    private val binding
        get() = _binding!!

    private lateinit var filtroViewModel:
            FiltroTrabajosViewModel

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        filtroViewModel =
            ViewModelProvider(requireActivity())[
                FiltroTrabajosViewModel::class.java
            ]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            BottomSheetFiltroTrabajosBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        restaurarFiltros()

        configurarBotonesEstado()

        configurarBotonesProgramacion()

        binding.btnQuitarFiltros.setOnClickListener {

            filtroViewModel.limpiar()

            restaurarFiltros()
        }

        binding.btnCerrar.setOnClickListener {
            dismiss()
        }
    }

    private fun restaurarFiltros() {

        val estados =
            filtroViewModel
                .estadosSeleccionados
                .value

        binding.btnEnviado.isChecked =
            EstadoTrabajo.ENVIADO in estados

        binding.btnAprobado.isChecked =
            EstadoTrabajo.APROBADO in estados

        binding.btnRechazado.isChecked =
            EstadoTrabajo.RECHAZADO in estados

        val programacion =
            filtroViewModel
                .programacionSeleccionada
                .value

        binding.btnProgramado.isChecked =
            FiltroTrabajosViewModel
                .FiltroProgramacion
                .PROGRAMADO in programacion

        binding.btnSinProgramar.isChecked =
            FiltroTrabajosViewModel
                .FiltroProgramacion
                .SIN_PROGRAMAR in programacion
    }

    private fun configurarBotonesEstado() {

        configurarEstado(
            binding.btnEnviado,
            EstadoTrabajo.ENVIADO
        )

        configurarEstado(
            binding.btnAprobado,
            EstadoTrabajo.APROBADO
        )

        configurarEstado(
            binding.btnRechazado,
            EstadoTrabajo.RECHAZADO
        )
    }

    private fun configurarEstado(
        boton: MaterialButton,
        estado: EstadoTrabajo
    ) {

        boton.setOnClickListener {

            filtroViewModel.alternarEstado(
                estado
            )
        }
    }

    private fun configurarBotonesProgramacion() {

        binding.btnProgramado.setOnClickListener {

            filtroViewModel.alternarProgramacion(
                FiltroTrabajosViewModel
                    .FiltroProgramacion
                    .PROGRAMADO
            )
        }

        binding.btnSinProgramar.setOnClickListener {

            filtroViewModel.alternarProgramacion(
                FiltroTrabajosViewModel
                    .FiltroProgramacion
                    .SIN_PROGRAMAR
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}