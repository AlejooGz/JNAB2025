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
import com.example.jnab2025.databinding.FragmentMisSimposiosBinding
import com.example.jnab2025.models.Simposio
import com.example.jnab2025.ui.adapters.MisSimposiosAdapter
import com.example.jnab2025.ui.viewmodels.SimposioViewModel

class MisSimposiosFragment : Fragment() {

    private var _binding: FragmentMisSimposiosBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: MisSimposiosAdapter
    private val simposioViewModel: SimposioViewModel by viewModels()
    // private val listaSimposios = mutableListOf<Simposio>()

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
        // cargarDatosEjemplo()

        simposioViewModel.simposios.observe(viewLifecycleOwner, Observer { simposios ->
            adapter = MisSimposiosAdapter(
                simposios = simposios,
                onEditarClick = { simposio ->
                    val action = MisSimposiosFragmentDirections
                        .actionMisSimposiosFragmentToEditarSimposioFragment(simposio.id)
                    findNavController().navigate(action)
                },
                onVerPropuestasClick = { simposio ->
                    val action = MisSimposiosFragmentDirections
                        .actionMisSimposiosFragmentToPropuestasFragment(simposio.id)
                    findNavController().navigate(action)
                }
            )
            binding.rvMisSimposios.adapter = adapter
        })

        // binding.rvMisSimposios.adapter = adapter

        // Configurar botón para crear nuevo simposio
        binding.btnNuevoSimposio.setOnClickListener {
            findNavController().navigate(
                MisSimposiosFragmentDirections.actionMisSimposiosFragmentToCrearSimposioFragment()
            )
        }
    }

    /* private fun cargarDatosEjemplo() {
        val datos = listOf(
            Simposio(0, "Raíces de la Humanidad: Avances en Evolución y Diversidad", "2025-05-15", "2025-05-15"),
            Simposio(0, "Huella Genética: Nuevas Perspectivas en Evolución Humana", "2025-05-16", "2025-05-16"),
            Simposio(0, "Genes, Cultura y Ambiente: Intersecciones Biológicas del Ser Humano", "2025-05-15", "2025-05-16"),
            Simposio(0, "Huesos y Memoria: Lecturas Biológicas del Registro Arqueológico", "2025-05-16", "2025-05-17"),
            Simposio(0, "Tecnologías Emergentes para el Estudio Biológico del Ser Humano", "2025-05-17", "2025-05-17"),
        )
        simposioViewModel.insertarTodos(datos)
    } */

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}