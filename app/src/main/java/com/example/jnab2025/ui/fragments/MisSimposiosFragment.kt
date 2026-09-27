package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.FragmentMisSimposiosBinding
import com.example.jnab2025.ui.adapters.MisSimposiosAdapter
import com.example.jnab2025.ui.viewmodels.MisSimposiosViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MisSimposiosFragment : Fragment() {
    private var _binding: FragmentMisSimposiosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MisSimposiosViewModel by viewModels()
    private lateinit var adapter: MisSimposiosAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMisSimposiosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MisSimposiosAdapter(

            onEditarClick = { simposio ->

                val accion =
                    MisSimposiosFragmentDirections
                        .actionMisSimposiosFragmentToEditarSimposioFragment(
                            simposio.id
                        )

                findNavController().navigate(accion)
            },

            onVerPropuestasClick = { simposio ->

                val accion =
                    MisSimposiosFragmentDirections
                        .actionMisSimposiosFragmentToPropuestasFragment(
                            simposio.id
                        )

                findNavController().navigate(accion)
            },

            onVerTrabajosClick = { simposio ->

                val accion =
                    MisSimposiosFragmentDirections
                        .actionMisSimposiosFragmentToTrabajosSimposioFragment(
                            simposio.id
                        )

                findNavController().navigate(accion)
            }
        )
        binding.rvMisSimposios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMisSimposios.adapter = adapter

        binding.btnNuevoSimposio.setOnClickListener {
            findNavController().navigate(
                MisSimposiosFragmentDirections.actionMisSimposiosFragmentToCrearSimposioFragment()
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filas.collectLatest { filas ->
                    adapter.submitList(filas)

                    /* Antes esto era un Toast, y el listener de Firestore emite
                     * varias veces: al entrar sin simposios aparecia repetido. */
                    binding.tvVacio.visibility =
                        if (filas.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvMisSimposios.adapter = null
        _binding = null
    }
}
