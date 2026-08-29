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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.FragmentSimposioBinding
import com.example.jnab2025.ui.adapters.SimposioAdapter
import com.example.jnab2025.ui.viewmodels.SimposioViewModel
import kotlinx.coroutines.launch

class SimposiosFragment : Fragment() {

    private var _binding: FragmentSimposioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SimposioViewModel by viewModels()

    private lateinit var adapter: SimposioAdapter

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
        super.onViewCreated(view, savedInstanceState)

        adapter = SimposioAdapter(emptyList())

        binding.rvEventos.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvEventos.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.simposios.collect { simposios ->
                    adapter.actualizarLista(simposios)
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