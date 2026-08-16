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
import com.example.jnab2025.databinding.FragmentVerInscriptosBinding
import com.example.jnab2025.ui.adapters.InscriptosAdapter
import com.example.jnab2025.ui.viewmodels.InscriptosViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Los inscriptos al evento, leidos de la tabla inscripcion. */
class VerInscriptosFragment : Fragment() {

    private var _binding: FragmentVerInscriptosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InscriptosViewModel by viewModels()
    private val adapter = InscriptosAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVerInscriptosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerInscriptos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerInscriptos.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.inscriptos.collectLatest { inscriptos ->
                    adapter.submitList(inscriptos)
                    binding.tvSinInscriptos.visibility =
                        if (inscriptos.isEmpty()) View.VISIBLE else View.GONE
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
