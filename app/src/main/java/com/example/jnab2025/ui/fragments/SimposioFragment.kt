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
import com.example.jnab2025.databinding.FragmentSimposioBinding
import com.example.jnab2025.ui.adapters.SimposioAdapter
import com.example.jnab2025.ui.viewmodels.SimposioViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SimposiosFragment : Fragment() {
    private var _binding: FragmentSimposioBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SimposioAdapter
    private val simposioViewModel: SimposioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSimposioBinding.inflate(
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
        configurarRecycler()
        observarSimposios()
    }

    private fun configurarRecycler() {
        adapter = SimposioAdapter()
        binding.rvEventos.layoutManager =
            LinearLayoutManager(requireContext()
            )
        binding.rvEventos.adapter = adapter
    }

    private fun observarSimposios() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                launch {
                    simposioViewModel
                        .simposios
                        .collectLatest { simposios ->
                            adapter.submitList(
                                simposios
                            )
                            binding.tvVacio.visibility =
                                if (simposios.isEmpty()) {
                                    View.VISIBLE
                                } else {
                                    View.GONE
                                }
                        }
                }

                /* La lista vacia significa dos cosas distintas y conviene
                 * distinguirlas: que no hay ningun simposio cargado, o que hay
                 * pero ninguno tiene todavia una charla programada. */
                launch {
                    simposioViewModel
                        .hayOcultos
                        .collectLatest { hayOcultos ->
                            binding.tvVacio.text =
                                if (hayOcultos) {
                                    "Todavía no hay simposios con charlas confirmadas. " +
                                            "Van a aparecer acá en cuanto la organización " +
                                            "programe las presentaciones."
                                } else {
                                    "Todavía no hay simposios publicados."
                                }
                        }
                }

                launch {
                    simposioViewModel
                        .error
                        .collectLatest { mensaje ->
                            if (mensaje != null) {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvEventos.adapter = null
        _binding = null
    }
}