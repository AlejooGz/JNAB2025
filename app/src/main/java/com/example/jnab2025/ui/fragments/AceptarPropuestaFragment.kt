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
import com.example.jnab2025.data.model.Charla
import com.example.jnab2025.databinding.FragmentAceptarPropuestaBinding
import com.example.jnab2025.ui.viewmodels.PropuestasViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Aprueba una propuesta y le da dia, hora y aula. Antes este fragment era el
 * template vacio de Android Studio: aceptar una propuesta no hacia nada, y por
 * eso ninguna charla llegaba nunca al cronograma.
 */
class AceptarPropuestaFragment : Fragment() {

    private var _binding: FragmentAceptarPropuestaBinding? = null
    private val binding get() = _binding!!

    private val args: AceptarPropuestaFragmentArgs by navArgs()
    private val viewModel: PropuestasViewModel by viewModels()

    private var fecha: LocalDate? = null
    private var hora: LocalTime? = null

    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAceptarPropuestaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.cargarTrabajo(args.trabajoId)

        binding.btnFecha.setOnClickListener { elegirFecha() }
        binding.btnHora.setOnClickListener { elegirHora() }
        binding.btnCancelar.setOnClickListener { findNavController().popBackStack() }

        binding.btnConfirmar.setOnClickListener {
            val dia = fecha
            val desde = hora
            if (dia == null || desde == null) {
                avisar("Elegi el dia y la hora")
                return@setOnClickListener
            }
            viewModel.aprobar(args.trabajoId, dia, desde)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.trabajo.collectLatest { trabajo ->
                        binding.tvTituloTrabajo.text = trabajo?.titulo.orEmpty()
                    }
                }
                launch {
                    viewModel.simposio.collectLatest { simposio ->
                        simposio ?: return@collectLatest
                        // El simposio ya define el aula, asi que el organizador
                        // solo elige cuando.
                        binding.tvSimposio.text = buildString {
                            append(simposio.titulo)
                            append("\nDel ${simposio.fechaInicio.format(formatoFecha)}")
                            append(" al ${simposio.fechaFin.format(formatoFecha)}")
                        }
                        if (fecha == null) fecha = simposio.fechaInicio
                        pintarSeleccion()
                    }
                }
                launch {
                    viewModel.avisos.collectLatest { avisar(it) }
                }
                launch {
                    viewModel.resueltas.collectLatest { findNavController().popBackStack() }
                }
            }
        }
    }

    private fun elegirFecha() {
        val base = fecha ?: LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, anio, mes, dia ->
                fecha = LocalDate.of(anio, mes + 1, dia)
                pintarSeleccion()
            },
            base.year, base.monthValue - 1, base.dayOfMonth
        ).show()
    }

    private fun elegirHora() {
        val base = hora ?: LocalTime.of(14, 0)
        TimePickerDialog(
            requireContext(),
            { _, h, m ->
                hora = LocalTime.of(h, m)
                pintarSeleccion()
            },
            base.hour, base.minute, true
        ).show()
    }

    private fun pintarSeleccion() {
        val dia = fecha
        val desde = hora
        binding.tvSeleccion.text = when {
            dia == null -> "Todavia no elegiste dia ni hora"
            desde == null -> "${dia.format(formatoFecha)} — falta la hora"
            else -> {
                val hasta = desde.plusMinutes(Charla.MINUTOS_PRESENTACION.toLong())
                "${dia.format(formatoFecha)} de $desde a $hasta"
            }
        }
    }

    private fun avisar(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
