package com.example.jnab2025.ui.fragments

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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.databinding.FragmentProgramarPresentacionBinding
import com.example.jnab2025.ui.adapters.SlotHorarioAdapter
import com.example.jnab2025.ui.viewmodels.CharlaViewModel
import com.example.jnab2025.utils.mostrarCargando
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Programa una presentacion dentro del simposio.
 *
 * Ya no se elige el dia: el simposio dura un dia y la charla va si o si en ese.
 * Y la hora se elige de una lista de bloques de 30 minutos, con los ocupados
 * a la vista pero no seleccionables, en lugar de un reloj libre donde se podia
 * poner cualquier horario y recien despues enterarse de que estaba tomado.
 */
class ProgramarPresentacionFragment : Fragment() {

    private var _binding: FragmentProgramarPresentacionBinding? = null
    private val binding get() = _binding!!

    private val args: ProgramarPresentacionFragmentArgs by navArgs()
    private val viewModel: CharlaViewModel by viewModels()

    private lateinit var adapter: SlotHorarioAdapter

    private var fechaDelSimposio: LocalDate? = null
    private var horaSeleccionada: LocalTime? = null

    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProgramarPresentacionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.cargarTrabajo(args.trabajoId)

        adapter = SlotHorarioAdapter { slot ->
            horaSeleccionada = slot.inicio
            adapter.seleccionar(slot.inicio)
            pintarSeleccion()
        }
        binding.rvSlots.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSlots.adapter = adapter

        binding.btnProgramar.setOnClickListener { programar() }
        binding.btnCancelar.setOnClickListener { findNavController().popBackStack() }

        observarDatos()
    }

    private fun programar() {
        val fecha = fechaDelSimposio
        val hora = horaSeleccionada

        if (fecha == null) {
            avisar("Todavía no se pudo leer la fecha del simposio")
            return
        }
        if (hora == null) {
            avisar("Elegí un horario de la lista")
            return
        }

        viewModel.programarPresentacion(
            trabajoId = args.trabajoId,
            fecha = fecha,
            horaInicio = hora
        )
    }

    private fun observarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.trabajo.collectLatest { trabajo ->
                        binding.tvTituloTrabajo.text = trabajo?.titulo.orEmpty()
                        binding.tvExpositor.text = trabajo
                            ?.autorNombre
                            ?.let { "Expositor: $it" }
                            .orEmpty()
                    }
                }

                launch {
                    viewModel.simposio.collectLatest { simposio ->
                        simposio ?: return@collectLatest

                        val fecha = simposio.fechaInicio
                            ?.toDate()
                            ?.toInstant()
                            ?.atZone(ZoneId.systemDefault())
                            ?.toLocalDate()

                        binding.tvSimposio.text = buildString {
                            append(simposio.titulo)
                            append("\nAula: ${simposio.aulaNombre}")
                            fecha?.let { append("\nDía: ${it.format(formatoFecha)}") }
                        }
                    }
                }

                launch {
                    viewModel.fechaDelSimposio.collectLatest { fecha ->
                        fechaDelSimposio = fecha
                        pintarSeleccion()
                    }
                }

                launch {
                    viewModel.slots.collectLatest { slots ->
                        adapter.submitList(slots)

                        val sinLibres = slots.none { it.libre }
                        binding.tvSinSlots.visibility =
                            if (slots.isEmpty() || sinLibres) View.VISIBLE else View.GONE
                        binding.tvSinSlots.text = when {
                            slots.isEmpty() -> "No hay horarios para mostrar"
                            else -> "No queda ningún horario libre en esta aula"
                        }

                        // Si el horario elegido se ocupo mientras tanto, se suelta.
                        val elegido = horaSeleccionada
                        if (elegido != null && slots.none { it.inicio == elegido && it.libre }) {
                            horaSeleccionada = null
                            adapter.seleccionar(null)
                            pintarSeleccion()
                        }
                    }
                }

                launch {
                    viewModel.enviando.collectLatest { mostrarCargando(it, "Programando…") }
                }

                launch {
                    viewModel.avisos.collectLatest { avisar(it) }
                }

                launch {
                    viewModel.programadas.collectLatest { findNavController().popBackStack() }
                }
            }
        }
    }

    private fun pintarSeleccion() {
        val fecha = fechaDelSimposio
        val inicio = horaSeleccionada

        binding.tvSeleccion.text = when {
            fecha == null -> "Cargando el día del simposio…"
            inicio == null -> "${fecha.format(formatoFecha)} — elegí un horario"
            else -> {
                val fin = inicio.plusMinutes(
                    CharlaFirebase.MINUTOS_PRESENTACION.toLong()
                )
                "${fecha.format(formatoFecha)} · $inicio a $fin"
            }
        }
    }

    private fun avisar(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mostrarCargando(false)
        binding.rvSlots.adapter = null
        _binding = null
    }
}
