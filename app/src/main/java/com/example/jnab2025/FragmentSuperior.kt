package com.example.jnab2025

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.jnab2025.databinding.FragmentSuperiorBinding
import java.text.SimpleDateFormat
import java.util.*

class FragmentSuperior : Fragment() {
    private var _binding: FragmentSuperiorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSuperiorBinding.inflate(inflater, container, false)

        val nombreUsuario = "Usuario"  // Si quieres, podrías recibirlo de argumentos
        val horaActual = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        binding.textViewSaludo.text = "Hola, $nombreUsuario! Son las $horaActual."

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}