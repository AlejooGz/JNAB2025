package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.models.Simposio
import com.example.jnab2025.ui.adapters.SimposioAdapter
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.jnab2025.databinding.FragmentSimposioBinding
import com.example.jnab2025.ui.viewmodels.SimposioViewModel // Ajusta el import

class SimposiosFragment : Fragment() {

    private var _binding: FragmentSimposioBinding? = null
    private val binding get() = _binding!!

    // private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SimposioAdapter
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
        // recyclerView = view.findViewById(R.id.rvEventos)
        // recyclerView.layoutManager = LinearLayoutManager(requireContext())
        super.onViewCreated(view, savedInstanceState)

        binding.rvEventos.layoutManager = LinearLayoutManager(requireContext())

        adapter = SimposioAdapter(emptyList())
        binding.rvEventos.adapter = adapter
        // recyclerView.adapter = adapter

        simposioViewModel.simposios.observe(viewLifecycleOwner, Observer { simposios ->
            adapter = SimposioAdapter(simposios)
            binding.rvEventos.adapter = adapter
            // recyclerView.adapter = adapter
        })

        // Si deseas insertar los datos de ejemplo al iniciar
        // simposioViewModel.insertarTodos(SimposioFakeData.getSimposiosDeEjemplo())
        //cargarDatosEjemplo()
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
