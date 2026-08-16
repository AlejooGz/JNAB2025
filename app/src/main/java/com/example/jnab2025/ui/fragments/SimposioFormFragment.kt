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
import com.example.jnab2025.data.model.Aula
import com.example.jnab2025.databinding.FragmentSimposioFormBinding
import com.example.jnab2025.ui.viewmodels.SimposioFormViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Formulario compartido por crear y editar simposio. La unica diferencia es si
 * llega con un id existente o con 0.
 */
abstract class SimposioFormFragment : Fragment() {

    private var _binding: FragmentSimposioFormBinding? = null
    protected val binding get() = _binding!!

    protected val viewModel: SimposioFormViewModel by viewModels()

    /** 0 cuando se esta creando uno nuevo. */
    protected abstract val simposioId: Long
    protected abstract val encabezado: String

    private var aulaId: Long? = null
    private var desde: LocalDate? = null
    private var hasta: LocalDate? = null

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

        if (simposioId != 0L) viewModel.cargar(simposioId)

        binding.btnAula.setOnClickListener { elegirAula() }
        binding.btnFechaInicio.setOnClickListener { elegirFecha(esInicio = true) }
        binding.btnFechaFin.setOnClickListener { elegirFecha(esInicio = false) }

        binding.btnGuardar.setOnClickListener {
            viewModel.guardar(
                simposioId = simposioId,
                titulo = binding.etTitulo.text.toString(),
                tema = binding.etTema.text.toString(),
                descripcion = binding.etDescripcion.text.toString(),
                aulaId = aulaId,
                desde = desde,
                hasta = hasta
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
                        desde = simposio.fechaInicio
                        hasta = simposio.fechaFin
                        pintarFechas()
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

    private fun elegirFecha(esInicio: Boolean) {
        val base = (if (esInicio) desde else hasta) ?: LocalDate.now()
        DatePickerDialog(
            requireContext(),
            { _, anio, mes, dia ->
                val elegida = LocalDate.of(anio, mes + 1, dia)
                if (esInicio) desde = elegida else hasta = elegida
                pintarFechas()
            },
            base.year, base.monthValue - 1, base.dayOfMonth
        ).show()
    }

    private fun pintarAula() {
        val id = aulaId
        val aula = viewModel.aulas.value.firstOrNull { it.id == id }
        binding.tvAula.text = aula?.let { describir(it) } ?: "Sin aula asignada"
    }

    private fun pintarFechas() {
        val d = desde
        val h = hasta
        binding.tvFechas.text = when {
            d == null && h == null -> "Sin fechas elegidas"
            d != null && h == null -> "Desde ${d.format(formato)}"
            d == null && h != null -> "Hasta ${h.format(formato)}"
            else -> "${d!!.format(formato)} al ${h!!.format(formato)}"
        }
    }

    private fun describir(aula: Aula) =
        if (aula.piso == 0) "${aula.nombre} - planta baja" else "${aula.nombre} - piso ${aula.piso}"

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
