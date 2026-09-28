package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentSimposiosFiltroHomeBinding
import com.example.jnab2025.ui.adapters.SimposioPickerAdapter
import com.example.jnab2025.ui.viewmodels.HomeOrganizadorViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SimposiosFiltroHomeFragment :
    Fragment() {
    private var _binding: FragmentSimposiosFiltroHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeOrganizadorViewModel
            by activityViewModels()
    private lateinit var adapter: SimposioPickerAdapter
    private val tipoFiltro: String
        get() =
            arguments
                ?.getString("tipoFiltro")
                .orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentSimposiosFiltroHomeBinding.inflate(
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
        configurarAdapter()
        configurarTitulo()
        observarSimposios()
    }

    private fun configurarTitulo() {
        binding.tvTitulo.text =
            when (tipoFiltro) {
                "PROPUESTAS" ->
                    "Simposios con propuestas pendientes"
                "PROGRAMAR" ->
                    "Simposios con trabajos sin programar"
                else ->
                    "Simposios"
            }
    }

    private fun configurarAdapter() {
        adapter =
            SimposioPickerAdapter { simposio ->
                when (tipoFiltro) {
                    "PROPUESTAS" -> {
                        val argumentos =
                            Bundle().apply {
                                putString(
                                    "simposioId",
                                    simposio.id
                                )
                            }
                        findNavController().navigate(
                            R.id.propuestasFragment,
                            argumentos
                        )
                    }
                    "PROGRAMAR" -> {
                        val argumentos =
                            Bundle().apply {
                                putString(
                                    "simposioId",
                                    simposio.id
                                )
                            }
                        findNavController().navigate(
                            R.id.trabajosSimposioFragment,
                            argumentos
                        )
                    }
                }
            }
        binding.recyclerSimposios.layoutManager =
            LinearLayoutManager(
                requireContext()
            )
        binding.recyclerSimposios.adapter = adapter
    }

    private fun observarSimposios() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                when (tipoFiltro) {
                    "PROPUESTAS" -> {
                        launch {
                            homeViewModel
                                .simposiosConPropuestas
                                .collectLatest { simposios ->
                                    adapter.submitList(
                                        simposios
                                    )
                                    mostrarEstadoVacio(
                                        simposios.isEmpty(),
                                        "No hay propuestas pendientes."
                                    )
                                }
                        }
                    }
                    "PROGRAMAR" -> {
                        launch {
                            homeViewModel
                                .simposiosConTrabajosSinProgramar
                                .collectLatest { simposios ->
                                    adapter.submitList(
                                        simposios
                                    )
                                    mostrarEstadoVacio(
                                        simposios.isEmpty(),
                                        "No hay trabajos pendientes de programación."
                                    )
                                }
                        }
                    }
                }
            }
        }
    }

    private fun mostrarEstadoVacio(
        vacio: Boolean,
        mensaje: String
    ) {
        binding.tvVacio.visibility =
            if (vacio) {
                View.VISIBLE
            } else {
                View.GONE
            }
        binding.recyclerSimposios.visibility =
            if (vacio) {
                View.GONE
            } else {
                View.VISIBLE
            }
        binding.tvVacio.text =
            mensaje
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerSimposios.adapter = null
        _binding = null
    }
}