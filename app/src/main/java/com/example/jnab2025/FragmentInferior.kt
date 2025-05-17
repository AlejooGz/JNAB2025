package com.example.jnab2025

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jnab2025.databinding.FragmentInferiorBinding

class FragmentInferior : Fragment(){
    private var _binding: FragmentInferiorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInferiorBinding.inflate(inflater, container, false)

        binding.opcionPerfil.setOnClickListener {
            Toast.makeText(context, "Ir a Perfil", Toast.LENGTH_SHORT).show()
        }
        binding.opcionAgenda.setOnClickListener {
            Toast.makeText(context, "Ir a Agenda", Toast.LENGTH_SHORT).show()
        }
        binding.opcionConfiguracion.setOnClickListener {
            Toast.makeText(context, "Ir a Configuración", Toast.LENGTH_SHORT).show()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}