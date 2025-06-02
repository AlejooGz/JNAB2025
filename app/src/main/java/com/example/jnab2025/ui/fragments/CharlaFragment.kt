package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.FragmentCharlaBinding
import com.example.jnab2025.ui.adapters.CharlaAdapter
import com.example.jnab2025.ui.viewmodels.CharlaViewModel

class AgendaFragment : Fragment() {

    private var _binding: FragmentCharlaBinding? = null
    private val binding get() = _binding!!

    // ViewModel
    private val viewModel: CharlaViewModel by viewModels()

    private lateinit var adapter: CharlaAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharlaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarRecyclerView()
        configurarObservadores()
        configurarListeners()
    }

    private fun configurarRecyclerView() {
        adapter = CharlaAdapter(
            charlas = emptyList(),
            onFavoritoClick = { charla ->
                viewModel.toggleFavorito(charla)
            },
            onItemClick = { charla ->
                // Navegar al detalle de la charla
                val action = AgendaFragmentDirections
                    .actionAgendaFragmentToCharlaDetailFragment(charla.id)
                findNavController().navigate(action)
            }
        )

        binding.rvEventos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AgendaFragment.adapter
        }
    }

    private fun configurarObservadores() {
        // Observar cambios en la lista de charlas
        viewModel.charlas.observe(viewLifecycleOwner) { charlas ->
            adapter.actualizarCharlas(charlas)
        }

        // Observar mensajes
        viewModel.mensaje.observe(viewLifecycleOwner) { mensaje ->
            if (mensaje.isNotEmpty()) {
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
                viewModel.limpiarMensaje()
            }
        }

        // Observar estado del filtro (opcional, por si quieres cambiar el icono del FAB)
        viewModel.mostrandoSoloFavoritos.observe(viewLifecycleOwner) { soloFavoritos ->
            // Aquí podrías cambiar el icono del FAB si quisieras
            // Por ejemplo:
            // val iconResource = if (soloFavoritos) {
            //     android.R.drawable.ic_menu_sort_alphabetically
            // } else {
            //     android.R.drawable.ic_menu_sort_by_size
            // }
            // binding.fabFiltrar.setImageResource(iconResource)
        }
    }

    private fun configurarListeners() {
        binding.fabFiltrar.setOnClickListener {
            viewModel.toggleFiltroFavoritos()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}