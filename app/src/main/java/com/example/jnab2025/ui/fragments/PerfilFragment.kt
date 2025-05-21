package com.example.jnab2025.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jnab2025.databinding.FragmentPerfilBinding

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val sharedPref = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)
        val rol = sharedPref.getString("user_rol", null)

        if (username.isNullOrEmpty() || rol.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para ver tu perfil", Toast.LENGTH_SHORT).show()
            // Opcional: redirigir a login o volver atrás
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        binding.etUsername.setText(username)
        binding.etRol.setText(rol)

        binding.btnGuardar.setOnClickListener {
            val nuevoUsername = binding.etUsername.text.toString()
            val nuevoRol = binding.etRol.text.toString()

            if (nuevoUsername.isNotBlank() && nuevoRol.isNotBlank()) {
                sharedPref.edit()
                    .putString("username", nuevoUsername)
                    .putString("user_rol", nuevoRol)
                    .apply()

                Toast.makeText(requireContext(), "Datos actualizados", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Campos vacíos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
