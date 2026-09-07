package com.example.jnab2025.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.data.model.AulaFirebase
import com.example.jnab2025.databinding.FragmentSimposioFormBinding
import com.example.jnab2025.ui.viewmodels.SimposioFormViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.firebase.Timestamp
import java.time.ZoneId
/**
 * Formulario compartido por crear y editar simposio. La unica diferencia es si
 * llega con un id existente o con 0.
 */
abstract class SimposioFormFragment : Fragment() {
    private var _binding: FragmentSimposioFormBinding? = null
    protected val binding get() = _binding!!

    protected val viewModel: SimposioFormViewModel by viewModels()
    protected abstract val simposioId: String?
    protected abstract val encabezado: String

    private var aulaId: String? = null
    /** El simposio dura un solo dia. */
    private var fecha: LocalDate? = null
    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimposioFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvEncabezado.text = encabezado
        simposioId?.let { id ->
            viewModel.cargar(id)
        }
        binding.btnAula.setOnClickListener { elegirAula() }
        binding.btnFecha.setOnClickListener { elegirFecha() }

        binding.btnGuardar.setOnClickListener {
            viewModel.guardar(
                simposioId = simposioId,
                titulo = binding.etTitulo.text.toString(),
                tema = binding.etTema.text.toString(),
                descripcion = binding.etDescripcion.text.toString(),
                aulaId = aulaId,
                fecha = fecha
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.simposio.collectLatest { simposio ->
                        simposio ?: return@collectLatest
                        binding.etTitulo.setText(simposio.titulo)
                        binding.etTema.setText(simposio.temaCentral)
                        binding.etDescripcion.setText(simposio.descripcion)
                        aulaId = simposio.aulaId
                        // El simposio es de un dia: fechaInicio es ese dia.
                        fecha = timestampALocalDate(
                            simposio.fechaInicio
                        )
                        pintarFecha()
                        pintarAula()
                    }
                }
                launch {
                    viewModel.aulas.collectLatest { pintarAula() }
                }
                launch {
                    viewModel.avisos.collectLatest {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    }
                }
                launch {
                    viewModel.guardados.collectLatest { findNavController().popBackStack() }
                }
            }
        }
    }

    private fun elegirAula() {
        val aulas = viewModel.aulas.value
        if (aulas.isEmpty()) {
            Toast.makeText(requireContext(), "No hay aulas cargadas", Toast.LENGTH_SHORT).show()
            return
        }
        val nombres = aulas.map { describir(it) }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Aula del simposio")
            .setItems(nombres) { _, indice ->
                aulaId = aulas[indice].id
                pintarAula()
            }
            .show()
    }

    private fun elegirFecha() {
        val base = fecha ?: LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, anio, mes, dia ->
                fecha = LocalDate.of(anio, mes + 1, dia)
                pintarFecha()
            },
            base.year, base.monthValue - 1, base.dayOfMonth
        ).show()
    }

    private fun pintarAula() {
        val id = aulaId
        val aula = viewModel.aulas.value.firstOrNull { it.id == id }
        binding.tvAula.text = aula?.let { describir(it) } ?: "Sin aula asignada"
    }

    private fun pintarFecha() {
        val elegida = fecha
        binding.tvFechas.text = elegida
            ?.let { "Día del simposio: ${it.format(formato)}" }
            ?: "Sin fecha elegida"
    }

    private fun describir(aula: AulaFirebase) =
        if (aula.piso == 0) "${aula.nombre} - planta baja" else "${aula.nombre} - piso ${aula.piso}"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun timestampALocalDate(
        timestamp: com.google.firebase.Timestamp?
    ): LocalDate? {
        return timestamp
            ?.toDate()
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()
    }
}
