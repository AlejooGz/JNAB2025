package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentMisSimposiosBinding
import com.example.jnab2025.models.Simposio
import com.example.jnab2025.ui.adapters.MisSimposiosAdapter

class MisSimposiosFragment : Fragment() {

    private var _binding: FragmentMisSimposiosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MisSimposiosAdapter
    private val listaSimposios = mutableListOf<Simposio>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMisSimposiosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar RecyclerView
        binding.rvMisSimposios.layoutManager = LinearLayoutManager(requireContext())
        cargarDatosEjemplo()

        adapter = MisSimposiosAdapter(
            simposios = listaSimposios,
            onEditarClick = { simposio ->
                // Navegar al fragment de edición
                val action = MisSimposiosFragmentDirections.actionMisSimposiosFragmentToEditarSimposioFragment(simposio.id)
                findNavController().navigate(action)
            },
            onVerPropuestasClick = { simposio ->
                // Navegar al fragment de propuestas
                val action = MisSimposiosFragmentDirections.actionMisSimposiosFragmentToPropuestasFragment(simposio.id)
                findNavController().navigate(action)
            }
        )

        binding.rvMisSimposios.adapter = adapter

        // Configurar botón para crear nuevo simposio
        binding.btnNuevoSimposio.setOnClickListener {
            findNavController().navigate(R.id.action_misSimposiosFragment_to_crearSimposioFragment)
        }
    }

    private fun cargarDatosEjemplo() {
        // Datos de ejemplo - Estos serían reemplazados por datos reales
        listaSimposios.apply {
            clear()
            add(
                Simposio(
                    id = 1,
                    titulo = "Raíces de la Humanidad: Avances en Evolución y Diversidad",
                    fechaInicio = "2025-05-15",
                    fechaFin = "2025-05-15"
                )
            )
            add(
                Simposio(
                    id = 2,
                    titulo = "Huella Genética: Nuevas Perspectivas en Evolución Humana",
                    fechaInicio = "2025-05-16",
                    fechaFin = "2025-05-16"
                )
            )
            add(
                Simposio(
                    id = 3,
                    titulo = "Genes, Cultura y Ambiente: Intersecciones Biológicas del Ser Humano",
                    fechaInicio = "2025-05-15",
                    fechaFin = "2025-05-16"
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}