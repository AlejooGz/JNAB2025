package com.example.jnab2025

import android.content.Context
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
        val sharedPref = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val rol = sharedPref.getString("user_rol", "invitado") ?: "invitado"

        if (rol == "expositor") {
            configurarVistaExpositor()
        } else {
            configurarVistaComun()
        }

        // botones comunes
        binding.btnListSimposios.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_simposiosFragment)
        }

        binding.btnVerAgenda.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_agendaFragment)
        }

        binding.btnFeedNovedades.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_novedadesFragment)
        }

        binding.btnMisSimposios.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_misSimposiosFragment)
        }
    }

    private fun configurarVistaExpositor() {
        Toast.makeText(requireContext(), "Bienvenido Expositor", Toast.LENGTH_SHORT).show()

        binding.btnTramite.visibility = View.VISIBLE
        binding.btnMisTramites.visibility = View.VISIBLE

        binding.btnTramite.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_tramiteExpositorFragment)
        }

        binding.btnMisTramites.setOnClickListener {
            findNavController().navigate(R.id.action_mainFragment_to_seguimientoTramiteFragment)
        }
    }


    private fun configurarVistaComun() {
        Toast.makeText(requireContext(), "Bienvenido", Toast.LENGTH_SHORT).show()

        // ocultar botones del expositor por si venía visible
        binding.btnTramite.visibility = View.GONE
        binding.btnMisTramites.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}