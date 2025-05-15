package com.example.jnab2025

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.FragmentFeedNovedadesBinding

class NovedadesFragment : Fragment() {

    private lateinit var binding: FragmentFeedNovedadesBinding
    private lateinit var adapter: NovedadesAdapter
    private val novedadesList = mutableListOf<Novedad>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentFeedNovedadesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.recyclerViewNovedades.layoutManager = LinearLayoutManager(requireContext())
        adapter = NovedadesAdapter(novedadesList)
        binding.recyclerViewNovedades.adapter = adapter
        cargarDatosIniciales()
    }

    private fun cargarDatosIniciales() {
        novedadesList.apply {
            add(Novedad("Cambio de aula", "El Simposio 3 se pasa al aula 27", "27/04/2025", R.drawable.aula))
            add(Novedad("Inicio de inscripciones", "Desde hoy se abren las inscripciones a expositores", "20/04/2025", R.drawable.inscripciones))
            add(Novedad("Coffee Break", "Habrá coffee break a las 16:00hs", "28/04/2025", R.drawable.breakcoffee))
        }
        adapter.notifyDataSetChanged()
    }

    private fun agregarNuevaNovedad(novedad: Novedad) {
        novedadesList.add(novedad)
        adapter.notifyItemInserted(novedadesList.size - 1)
    }
}

