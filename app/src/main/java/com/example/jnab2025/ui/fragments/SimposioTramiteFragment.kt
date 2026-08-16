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
import com.example.jnab2025.databinding.FragmentElegirSimposioBinding
import com.example.jnab2025.ui.adapters.SimposioPickerAdapter
import com.example.jnab2025.ui.viewmodels.EnviarTrabajoViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Primer paso del envio: elegir a que simposio va el trabajo. */
class SimposioTramiteFragment : Fragment() {

    private var _binding: FragmentElegirSimposioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EnviarTrabajoViewModel by viewModels()
    private lateinit var adapter: SimposioPickerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentElegirSimposioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SimposioPickerAdapter { simposio ->
            val accion = SimposioTramiteFragmentDirections
                .actionSimposiosTramiteFragmentToTramiteExpositorFragment(simposio.id)
            findNavController().navigate(accion)
        }
        binding.rvSimposios.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSimposios.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.simposios.collectLatest { simposios ->
                    adapter.submitList(simposios)
                    binding.tvVacio.visibility =
                        if (simposios.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvSimposios.adapter = null
        _binding = null
    }
}
