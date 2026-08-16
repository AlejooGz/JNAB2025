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
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.TipoInscripcion
import com.example.jnab2025.databinding.FragmentInscripcionBinding
import com.example.jnab2025.ui.viewmodels.InscripcionViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Estado de mi inscripcion al evento. Antes esta pantalla era un formulario que
 * guardaba un JSON en SharedPreferences que despues nadie leia.
 */
class InscripcionFragment : Fragment() {

    private var _binding: FragmentInscripcionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InscripcionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInscripcionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rgTipo.setOnCheckedChangeListener { _, _ -> pintarMonto() }

        binding.btnInscribirse.setOnClickListener {
            viewModel.inscribirse(tipoElegido())
        }

        binding.btnComprobante.setOnClickListener {
            findNavController().navigate(R.id.cargarComprobanteFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.vista.collectLatest { pintar(it) }
                }
                launch {
                    viewModel.avisos.collectLatest {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun pintar(vista: InscripcionViewModel.Vista) {
        binding.tvEvento.text = vista.evento?.nombre.orEmpty()
        pintarMonto()

        val inscripcion = vista.inscripcion
        if (inscripcion == null) {
            binding.tvEstado.text = "Todavia no estas inscripto a las jornadas."
            binding.cardInscribirse.visibility = View.VISIBLE
            binding.btnComprobante.visibility = View.GONE
            binding.tvComprobante.visibility = View.GONE
            return
        }

        // Ya inscripto: se oculta el formulario y se muestra el estado real.
        binding.cardInscribirse.visibility = View.GONE

        val tipo = inscripcion.tipo.name.lowercase()
        binding.tvEstado.text = when (inscripcion.estado) {
            EstadoInscripcion.PAGADA ->
                "Inscripcion de $tipo confirmada. No tenes nada pendiente."
            EstadoInscripcion.PENDIENTE_PAGO ->
                "Inscripcion de $tipo registrada. Falta acreditar el pago de $${inscripcion.monto.toInt()}."
            EstadoInscripcion.ANULADA ->
                "Tu inscripcion figura anulada. Contactate con la organizacion."
        }

        val comprobante = vista.comprobante
        binding.tvComprobante.visibility = if (comprobante == null) View.GONE else View.VISIBLE
        if (comprobante != null) {
            binding.tvComprobante.text = when (comprobante.estado) {
                EstadoComprobante.PENDIENTE ->
                    "Comprobante ${comprobante.nombreArchivo} enviado, esperando verificacion."
                EstadoComprobante.VERIFICADO ->
                    "Comprobante ${comprobante.nombreArchivo} verificado por la organizacion."
                EstadoComprobante.RECHAZADO ->
                    "El comprobante ${comprobante.nombreArchivo} fue rechazado. Carga otro."
            }
        }

        // Puede cargar (o reemplazar) el comprobante mientras no este acreditada.
        binding.btnComprobante.visibility =
            if (inscripcion.estado == EstadoInscripcion.PENDIENTE_PAGO) View.VISIBLE else View.GONE
        binding.btnComprobante.text =
            if (comprobante == null) "Cargar comprobante de pago" else "Reemplazar comprobante"
    }

    private fun tipoElegido() = when (binding.rgTipo.checkedRadioButtonId) {
        R.id.rbEstudiante -> TipoInscripcion.ESTUDIANTE
        R.id.rbExpositor -> TipoInscripcion.EXPOSITOR
        else -> TipoInscripcion.ASISTENTE
    }

    private fun pintarMonto() {
        val monto = viewModel.montoPara(tipoElegido())
        binding.tvMonto.text = monto?.let { "A pagar: $${it.toInt()}" } ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
