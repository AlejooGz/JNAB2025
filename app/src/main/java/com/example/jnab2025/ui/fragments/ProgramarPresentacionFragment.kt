package com.example.jnab2025.ui.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.databinding.FragmentProgramarPresentacionBinding
import com.example.jnab2025.ui.viewmodels.CharlaViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ProgramarPresentacionFragment : Fragment() {
    private var _binding: FragmentProgramarPresentacionBinding? = null
    private val binding get() = _binding!!
    private val args: ProgramarPresentacionFragmentArgs by navArgs()
    private val viewModel: CharlaViewModel by viewModels()
    private var fechaSeleccionada: LocalDate? = null
    private var horaSeleccionada: LocalTime? = null
    private val formatoFecha =
        DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentProgramarPresentacionBinding.inflate(
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
        super.onViewCreated(view, savedInstanceState)

        viewModel.cargarTrabajo(
            args.trabajoId
        )

        binding.btnFecha.setOnClickListener {
            elegirFecha()
        }

        binding.btnHora.setOnClickListener {
            elegirHora()
        }

        binding.btnProgramar.setOnClickListener {
            val fecha = fechaSeleccionada
            val hora = horaSeleccionada
            if (fecha == null || hora == null) {
                avisar(
                    "Elegí el día y la hora de la presentación")
                return@setOnClickListener
            }

            viewModel.programarPresentacion(
                trabajoId = args.trabajoId,
                fecha = fecha,
                horaInicio = hora
            )
        }
        binding.btnCancelar.setOnClickListener {
            findNavController()
                .popBackStack()
        }
        observarDatos()
    }

    private fun observarDatos() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch {
                    viewModel.trabajo.collectLatest { trabajo ->
                        binding.tvTituloTrabajo.text = trabajo?.titulo.orEmpty()
                        binding.tvExpositor.text =
                            trabajo
                                ?.autorNombre
                                ?.let {
                                    "Expositor: $it"
                                }
                                .orEmpty()
                    }
                }
                launch {
                    viewModel.simposio.collectLatest { simposio ->
                        simposio
                            ?: return@collectLatest
                        val desde =
                            simposio.fechaInicio
                                ?.toDate()
                                ?.toInstant()
                                ?.atZone(
                                    ZoneId.systemDefault()
                                )
                                ?.toLocalDate()

                        val hasta =
                            simposio.fechaFin
                                ?.toDate()
                                ?.toInstant()
                                ?.atZone(
                                    ZoneId.systemDefault()
                                )
                                ?.toLocalDate()

                        binding.tvSimposio.text =
                            buildString {
                                append(
                                    simposio.titulo
                                )
                                append(
                                    "\nAula: ${simposio.aulaNombre}"
                                )

                                if (
                                    desde != null &&
                                    hasta != null
                                ) {

                                    if (desde == hasta) {

                                        append(
                                            "\nFecha: ${
                                                desde.format(
                                                    formatoFecha
                                                )
                                            }"
                                        )

                                    } else {

                                        append(
                                            "\nDel ${
                                                desde.format(
                                                    formatoFecha
                                                )
                                            } al ${
                                                hasta.format(
                                                    formatoFecha
                                                )
                                            }"
                                        )
                                    }
                                }
                            }
                        if (
                            fechaSeleccionada == null &&
                            desde != null
                        ) {
                            fechaSeleccionada =
                                desde
                            pintarSeleccion()
                        }
                    }
                }
                launch {
                    viewModel.avisos.collectLatest {
                            mensaje ->
                        avisar(mensaje)
                    }
                }
                launch {
                    viewModel.programadas.collectLatest {
                        findNavController()
                            .popBackStack()
                    }
                }
            }
        }
    }

    private fun elegirFecha() {

        val base = fechaSeleccionada
                ?: LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, anio, mes, dia ->
                fechaSeleccionada =
                    LocalDate.of(
                        anio,
                        mes + 1,
                        dia
                    )
                pintarSeleccion()
            },
            base.year,
            base.monthValue - 1,
            base.dayOfMonth
        ).show()
    }
    private fun elegirHora() {
        val base =
            horaSeleccionada
                ?: LocalTime.of(
                    14,
                    0
                )
        TimePickerDialog(
            requireContext(),
            { _, hora, minuto ->
                horaSeleccionada =
                    LocalTime.of(
                        hora,
                        minuto
                    )
                pintarSeleccion()
            },
            base.hour,
            base.minute,
            true
        ).show()
    }

    private fun pintarSeleccion() {
        val fecha = fechaSeleccionada
        val inicio = horaSeleccionada
        binding.tvSeleccion.text =
            when {
                fecha == null ->
                    "Todavía no elegiste el día"
                inicio == null ->
                    "${fecha.format(formatoFecha)} — falta elegir la hora"
                else -> {
                    val fin =
                        inicio.plusMinutes(
                            CharlaFirebase
                                .MINUTOS_PRESENTACION
                                .toLong()
                        )
                    "${fecha.format(formatoFecha)} · $inicio a $fin"
                }
            }
    }
    private fun avisar(
        mensaje: String
    ) {
        Toast.makeText(
            requireContext(),
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}