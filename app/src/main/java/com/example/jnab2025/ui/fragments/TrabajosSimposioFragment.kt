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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.FragmentTrabajosSimposioBinding
import com.example.jnab2025.ui.adapters.TrabajosSimposioAdapter
import com.example.jnab2025.ui.viewmodels.TrabajosSimposioViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TrabajosSimposioFragment : Fragment() {
    private var _binding: FragmentTrabajosSimposioBinding? = null
    private val binding get() = _binding!!
    private val args: TrabajosSimposioFragmentArgs by navArgs()
    private val viewModel: TrabajosSimposioViewModel by viewModels()
    private lateinit var adapter: TrabajosSimposioAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentTrabajosSimposioBinding.inflate(
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
            TrabajosSimposioAdapter(
                onProgramarClick = { item ->

                    val accion =
                        TrabajosSimposioFragmentDirections
                            .actionTrabajosSimposioFragmentToProgramarPresentacionFragment(
                                item.trabajo.id
                            )

                    findNavController()
                        .navigate(accion)
                }
            )
        binding.rvTrabajos.layoutManager =
            LinearLayoutManager(
                requireContext()
            )
        binding.rvTrabajos.adapter = adapter
        viewModel.cargar(
            args.simposioId
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch {
                    viewModel.trabajos.collectLatest {
                            trabajos ->
                        adapter.submitList(
                            trabajos
                        )
                        binding.tvVacio.visibility =
                            if (
                                trabajos.isEmpty()
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
        binding.rvTrabajos.adapter = null
        _binding = null
    }
}