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
import com.example.jnab2025.databinding.FragmentCharlaBinding
import com.example.jnab2025.ui.adapters.CharlaFirebaseAdapter
import com.example.jnab2025.ui.viewmodels.CharlaViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CharlaFragment : Fragment() {
    private var _binding: FragmentCharlaBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CharlaViewModel by viewModels()
    private lateinit var adapter: CharlaFirebaseAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentCharlaBinding.inflate(
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
            CharlaFirebaseAdapter(
                onItemClick = { charla ->
                    Toast.makeText(
                        requireContext(),
                        charla.titulo,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        binding.rvEventos.layoutManager =
            LinearLayoutManager(
                requireContext()
            )
        binding.rvEventos.adapter = adapter
        binding.fabFiltrar.visibility =
            View.GONE
        observarDatos()
    }
    private fun observarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch {
                    viewModel.charlas.collectLatest { charlas ->
                        adapter.submitList(
                            charlas
                        )
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvEventos.adapter = null
        _binding = null
    }
}