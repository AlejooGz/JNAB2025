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
import com.example.jnab2025.databinding.FragmentSeguimientoTramiteBinding
import com.example.jnab2025.ui.adapters.MisTrabajosAdapter
import com.example.jnab2025.ui.viewmodels.MisTrabajosViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Seguimiento de los trabajos del expositor. Los datos salen de una sola
 * consulta que ya trae el estado, la programacion y si la inscripcion esta paga.
 */
class SeguimientoTramiteFragment : Fragment() {

    private var _binding: FragmentSeguimientoTramiteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MisTrabajosViewModel by viewModels()
    private lateinit var adapter: MisTrabajosAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeguimientoTramiteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!viewModel.haySesion) {
            Toast.makeText(
                requireContext(),
                "Inicia sesion para ver tus trabajos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        adapter = MisTrabajosAdapter { trabajo ->
            Toast.makeText(requireContext(), trabajo.titulo, Toast.LENGTH_SHORT).show()
        }
        binding.recyclerViewTramites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTramites.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.trabajos.collectLatest { adapter.submitList(it) }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewTramites.adapter = null
        _binding = null
    }
}
