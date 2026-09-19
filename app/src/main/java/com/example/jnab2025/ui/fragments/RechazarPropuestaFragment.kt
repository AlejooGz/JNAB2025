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
import com.example.jnab2025.databinding.FragmentRechazarPropuestaBinding
import com.example.jnab2025.ui.viewmodels.PropuestasViewModel
import com.example.jnab2025.utils.Archivos
import com.example.jnab2025.utils.mostrarCargando
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Rechaza una propuesta guardando el motivo. Antes mostraba una propuesta
 * inventada y el mensaje que escribia el organizador se descartaba.
 */
class RechazarPropuestaFragment : Fragment() {

    private var _binding: FragmentRechazarPropuestaBinding? = null
    private val binding get() = _binding!!
    private val args: RechazarPropuestaFragmentArgs by navArgs()
    private val viewModel: PropuestasViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRechazarPropuestaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.cargarTrabajo(args.trabajoId)

        binding.btnVerPdf.setOnClickListener {
            val trabajo = viewModel.trabajo.value
            Archivos.mensajeDe(
                Archivos.abrirPdf(
                    requireContext(),
                    trabajo?.archivoUrl
                )
            )?.let {
                Toast.makeText(
                    requireContext(),
                    it,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        binding.btnCancelarRechazo.setOnClickListener { findNavController().popBackStack() }

        binding.btnConfirmarRechazo.setOnClickListener {
            val motivo = binding.etMensajeRechazo.text.toString().trim()
            if (motivo.isEmpty()) {
                binding.tilMensajeRechazo.error = "Debe proporcionar un mensaje"
                return@setOnClickListener
            }
            binding.tilMensajeRechazo.error = null
            viewModel.rechazar(args.trabajoId, motivo)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.trabajo.collectLatest { trabajo ->
                        binding.tvPropuestaARechazar.text = trabajo?.titulo.orEmpty()
                        binding.btnVerPdf.text = trabajo?.nombreArchivo
                            ?.let { "Ver PDF: $it" } ?: "Ver PDF adjunto"
                    }
                }
                launch {
                    viewModel.enviando.collectLatest { mostrarCargando(it) }
                }
                launch {
                    viewModel.avisos.collectLatest {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    }
                }
                launch {
                    viewModel.resueltas.collectLatest { findNavController().popBackStack() }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mostrarCargando(false)
        _binding = null
    }
}
