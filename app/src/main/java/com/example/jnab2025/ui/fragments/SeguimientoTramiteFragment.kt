// === 1. SeguimientoTramiteFragment.kt ===
// Reemplazado para usar CharlaViewModel

package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentSeguimientoTramiteBinding
import com.example.jnab2025.models.EstadoPropuesta
import com.example.jnab2025.ui.adapters.CharlaExpositorAdapter
import com.example.jnab2025.ui.viewmodels.CharlaViewModel

class SeguimientoTramiteFragment : Fragment() {

    private var _binding: FragmentSeguimientoTramiteBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: CharlaExpositorAdapter
    private val charlaViewModel: CharlaViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeguimientoTramiteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        charlaViewModel.charlas.observe(viewLifecycleOwner) { charlas ->
            val delExpositor = charlas.filter { it.expositorId == 1 } // Reemplazar con ID real del expositor logueado
                .sortedBy { it.pagado }

            adapter = CharlaExpositorAdapter(
                charlas = delExpositor,
                onItemClick = { charla ->
                    val bundle = Bundle().apply {
                        putInt("charlaId", charla.id)
                        putString("tituloTrabajo", charla.titulo)
                    }
                    when (charla.estado) {
                        EstadoPropuesta.PENDIENTE -> findNavController().navigate(R.id.editarTramiteFragment, bundle)
                        EstadoPropuesta.APROBADA -> findNavController().navigate(R.id.cargarComprobanteFragment, bundle)
                        else -> Toast.makeText(requireContext(), "Esta charla no puede ser modificada", Toast.LENGTH_SHORT).show()
                    }
                },
                onSubirComprobanteClick = { charla ->
                    val bundle = Bundle().apply {
                        putInt("charlaId", charla.id)
                        putString("tituloTrabajo", charla.titulo)
                    }
                    findNavController().navigate(R.id.cargarComprobanteFragment, bundle)
                }
            )

            binding.recyclerViewTramites.layoutManager = LinearLayoutManager(requireContext())
            binding.recyclerViewTramites.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
