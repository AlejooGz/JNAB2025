package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.data.TramiteRepository
import com.example.jnab2025.databinding.FragmentSeguimientoTramiteBinding
import com.example.jnab2025.ui.adapters.TramiteAdapter


class SeguimientoTramiteFragment : Fragment() {

    private var _binding: FragmentSeguimientoTramiteBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TramiteAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeguimientoTramiteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tramites = TramiteRepository.obtenerTramites()
            .sortedBy { it.pagado }

        adapter = TramiteAdapter(
            tramites,
            onItemClick = { tramite ->
                val bundle = Bundle().apply {
                    putInt("tramiteId", tramite.id)
                    putString("tituloTrabajo", tramite.tituloTrabajo)
                }

                when (tramite.estado) {
                    "Pendiente" -> {
                        findNavController().navigate(R.id.editarTramiteFragment, bundle)
                    }
                    "Aceptado" -> {
                        findNavController().navigate(R.id.cargarComprobanteFragment, bundle)
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Este trabajo no puede ser editado ni modificado", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onSubirComprobanteClick = { tramite ->
                val bundle = Bundle().apply {
                    putInt("tramiteId", tramite.id)
                    putString("tituloTrabajo", tramite.tituloTrabajo)
                }
                findNavController().navigate(R.id.cargarComprobanteFragment, bundle)
            }
        )

        binding.recyclerViewTramites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTramites.adapter = adapter

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
