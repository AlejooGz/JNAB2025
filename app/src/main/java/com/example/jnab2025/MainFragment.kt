package com.example.jnab2025

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnListSimposios.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_simposiosFragment)
        }

        binding.btnVerAgenda.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_agendaFragment)
        }

        binding.btnFeedNovedades.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_novedadesFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}