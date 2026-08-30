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
import com.example.jnab2025.databinding.FragmentPropuestasBinding
import com.example.jnab2025.ui.adapters.PropuestasAdapter
import com.example.jnab2025.ui.viewmodels.PropuestasViewModel
import com.example.jnab2025.utils.Archivos
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PropuestasFragment : Fragment() {
    private var _binding: FragmentPropuestasBinding? = null
    private val binding get() = _binding!!
    private val args: PropuestasFragmentArgs by navArgs()
    private val viewModel: PropuestasViewModel by viewModels()
    private lateinit var adapter: PropuestasAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropuestasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PropuestasAdapter(
            onVerPdfClick = { propuesta -> abrirPdf(propuesta.archivoUrl) },
            onAceptarClick = { propuesta ->
                val accion = PropuestasFragmentDirections
                    .actionPropuestasFragmentToAceptarPropuestaFragment(propuesta.id)
                findNavController().navigate(accion)
            },
            onRechazarClick = { propuesta ->
                val accion = PropuestasFragmentDirections
                    .actionPropuestasFragmentToRechazarPropuestaFragment(propuesta.id)
                findNavController().navigate(accion)
            }
        )
        binding.rvPropuestas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPropuestas.adapter = adapter

        viewModel.cargarSimposio(args.simposioId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.simposio.collectLatest { simposio ->
                        binding.tvTituloSimposioActual.text = simposio?.titulo.orEmpty()
                    }
                }
                launch {
                    viewModel.propuestas.collectLatest { propuestas ->
                        adapter.submitList(propuestas)
                        val vacia = propuestas.isEmpty()
                        binding.tvNoPropuestas.visibility = if (vacia) View.VISIBLE else View.GONE
                        binding.rvPropuestas.visibility = if (vacia) View.GONE else View.VISIBLE
                    }
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

    //abre el PDF que adjunto el expositor, para poder revisarlo antes de decidir
    private fun abrirPdf(archivoUrl: String?) {
        Archivos.mensajeDe(
            Archivos.abrirPdf(
                requireContext(),
                archivoUrl
            )
        )?.let { mensaje ->
            Toast.makeText(
                requireContext(),
                mensaje,
                Toast.LENGTH_LONG
            ).show()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvPropuestas.adapter = null
        _binding = null
    }
}
