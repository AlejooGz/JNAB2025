package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentSimposioBinding
import com.example.jnab2025.ui.adapters.SimposioTramiteAdapter
import com.example.jnab2025.ui.viewmodels.SimposioViewModel
import kotlin.getValue

class SimposioTramiteFragment : Fragment() {
    private var _binding: FragmentSimposioBinding? = null
    private val binding get() = _binding!!

    // private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimposioTramiteAdapter
    private val simposioViewModel: SimposioViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?)
            : View {
        // return inflater.inflate(R.layout.fragment_simposio, container, false)
        _binding = FragmentSimposioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())

        adapter = SimposioTramiteAdapter(emptyList()) { simposioId ->
            // Acción por defecto al iniciar: podrías dejarlo vacío o con un Toast
        }
        binding.rvEventos.adapter = adapter

        simposioViewModel.simposios.observe(viewLifecycleOwner, Observer { simposios ->
            adapter = SimposioTramiteAdapter(simposios) { simposioId ->
                val action = SimposioTramiteFragmentDirections
                    .actionSimposiosTramiteFragmentToTramiteExpositorFragment(simposioId)
                findNavController().navigate(action)
            }
            binding.rvEventos.adapter = adapter
        })
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}