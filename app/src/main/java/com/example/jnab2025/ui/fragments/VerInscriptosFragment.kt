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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.FragmentVerInscriptosBinding
import com.example.jnab2025.ui.adapters.InscriptosAdapter
import com.example.jnab2025.ui.viewmodels.InscriptosViewModel
import com.example.jnab2025.utils.Archivos
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class VerInscriptosFragment : Fragment() {
    private var _binding: FragmentVerInscriptosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InscriptosViewModel by viewModels()
    private lateinit var adapter: InscriptosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentVerInscriptosBinding.inflate(
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
        adapter =
            InscriptosAdapter(
                onVerComprobante = { seguimiento ->
                    val resultado =
                        Archivos.abrirPdf(
                            requireContext(),
                            seguimiento
                                .comprobante
                                ?.archivoUrl
                        )
                    Archivos
                        .mensajeDe(resultado)
                        ?.let { mensaje ->

                            Toast.makeText(
                                requireContext(),
                                mensaje,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                onVerificar = { seguimiento ->
                    viewModel.verificar(
                        seguimiento
                    )
                },
                onRechazar = { seguimiento ->
                    viewModel.rechazar(
                        seguimiento
                    )
                }
            )
        binding.recyclerInscriptos.layoutManager =
            LinearLayoutManager(
                requireContext()
            )
        binding.recyclerInscriptos.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch {
                    viewModel.inscriptos.collectLatest {
                            inscriptos ->
                        adapter.submitList(
                            inscriptos
                        )
                        binding
                            .tvSinInscriptos
                            .visibility =
                            if (
                                inscriptos.isEmpty()
                            ) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                    }
                }
                launch {
                    viewModel.avisos.collectLatest {
                            mensaje ->

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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerInscriptos.adapter = null
        _binding = null
    }
}