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
import com.example.jnab2025.R
import com.example.jnab2025.data.model.CategoriaInscripcion
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.TipoInscripcion
import com.example.jnab2025.databinding.FragmentInscripcionBinding
import com.example.jnab2025.ui.viewmodels.InscripcionViewModel
import com.example.jnab2025.utils.mostrarCargando
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InscripcionFragment : Fragment() {
    private var _binding: FragmentInscripcionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InscripcionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentInscripcionBinding.inflate(
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
        binding.rgTipo.setOnCheckedChangeListener { _, _ ->
            pintarMonto()
        }
        binding.cbEstudiante.setOnCheckedChangeListener { _, _ ->
            pintarMonto()
        }
        binding.btnInscribirse.setOnClickListener {
            viewModel.inscribirse(
                tipo = tipoElegido(),
                categoria = categoriaElegida()
            )
        }
        binding.btnComprobante.setOnClickListener {
            findNavController().navigate(
                R.id.cargarComprobanteFragment
            )
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch {
                    viewModel.vista.collectLatest { vista ->
                        pintar(vista)
                    }
                }

                launch {
                    viewModel.enviando.collectLatest { mostrarCargando(it) }
                }

                launch {
                    viewModel.avisos.collectLatest { mensaje ->
                        Toast.makeText(
                            requireContext(),
                            mensaje,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun pintar(
        vista: InscripcionViewModel.Vista
    ) {
        binding.tvEvento.text =
            "Jornadas Nacionales de Antropología Biológica 2025"
        pintarMonto()
        val inscripcion = vista.inscripcion
        if (inscripcion == null) {
            binding.tvEstado.text =
                "Todavía no estás inscripto a las jornadas."

            binding.cardInscribirse.visibility = View.VISIBLE
            binding.btnComprobante.visibility = View.GONE
            binding.tvComprobante.visibility = View.GONE

            return
        }

        // Si ya existe inscripción no mostramos nuevamente el formulario.
        binding.cardInscribirse.visibility = View.GONE
        val tipo = inscripcion.tipo.lowercase()
        val categoria = inscripcion.categoria.lowercase()
        binding.tvEstado.text =
            when (inscripcion.estado) {
                EstadoInscripcion.PAGADA.name ->
                    "Inscripción de $tipo · $categoria confirmada. " +
                            "No tenés nada pendiente."
                EstadoInscripcion.PENDIENTE_PAGO.name ->
                    "Inscripción de $tipo · $categoria registrada. " +
                            "Falta acreditar el pago de " +
                            "$${inscripcion.monto.toInt()}."
                EstadoInscripcion.ANULADA.name ->
                    "Tu inscripción figura anulada. " +
                            "Contactate con la organización."
                else ->
                    "Estado de inscripción desconocido."
            }
        val comprobante = vista.comprobante
        binding.tvComprobante.visibility =
            if (comprobante == null) {
                View.GONE
            } else {
                View.VISIBLE
            }

        if (comprobante != null) {
            binding.tvComprobante.text =
                when (comprobante.estado) {
                    EstadoComprobante.PENDIENTE.name ->
                        "Comprobante ${comprobante.nombreArchivo} " +
                                "enviado, esperando verificación."

                    EstadoComprobante.VERIFICADO.name ->
                        "Comprobante ${comprobante.nombreArchivo} " +
                                "verificado por la organización."

                    EstadoComprobante.RECHAZADO.name ->
                        "El comprobante ${comprobante.nombreArchivo} " +
                                "fue rechazado. Cargá otro."
                    else ->
                        ""
                }
        }
        //Puede subir o reemplazar el comprobante mientras la inscripción siga pendiente.
        binding.btnComprobante.visibility =
            if (
                inscripcion.estado == EstadoInscripcion.PENDIENTE_PAGO.name
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.btnComprobante.text =
            if (comprobante == null) {
                "Cargar comprobante de pago"
            } else {
                "Reemplazar comprobante"
            }
    }
    private fun tipoElegido(): TipoInscripcion {
        return when (
            binding.rgTipo.checkedRadioButtonId
        ) {
            R.id.rbExpositor -> TipoInscripcion.EXPOSITOR
            else ->
                TipoInscripcion.ASISTENTE
        }
    }
    private fun categoriaElegida():
            CategoriaInscripcion {
        return if (
            binding.cbEstudiante.isChecked
        ) {
            CategoriaInscripcion.ESTUDIANTE
        } else {
            CategoriaInscripcion.GENERAL
        }
    }
    private fun pintarMonto() {
        val monto =
            viewModel.montoPara(
                categoriaElegida()
            )
        binding.tvMonto.text =
            "A pagar: $${monto.toInt()}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mostrarCargando(false)
        _binding = null
    }
}