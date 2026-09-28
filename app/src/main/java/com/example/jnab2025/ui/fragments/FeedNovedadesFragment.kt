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
import com.example.jnab2025.databinding.FragmentFeedNovedadesBinding
import com.example.jnab2025.ui.adapters.NovedadesAdapter
import com.example.jnab2025.ui.viewmodels.NovedadesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NovedadesFragment : Fragment() {
    private var _binding: FragmentFeedNovedadesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NovedadesViewModel by viewModels()
    private lateinit var adapter: NovedadesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedNovedadesBinding.inflate(
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
        configurarRecyclerView()
        observarNovedades()
    }

    private fun configurarRecyclerView() {
        adapter = NovedadesAdapter(
            esOrganizador = false,
            onEditar = {},
            onEliminar = {}
        )
        binding.recyclerViewNovedades.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@NovedadesFragment.adapter
        }
    }

    private fun observarNovedades() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch {
                    viewModel.novedades.collectLatest { novedades ->
                        adapter.submitList(novedades)
                    }
                }
                launch {
                    viewModel.eventos.collectLatest { evento ->
                        if (evento is NovedadesViewModel.Evento.Error) {
                            Toast.makeText(
                                requireContext(),
                                evento.mensaje,
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
        binding.recyclerViewNovedades.adapter = null
        _binding = null
    }
}
