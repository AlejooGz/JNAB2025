package com.example.jnab2025.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.databinding.FragmentInscripcionBinding
import com.example.jnab2025.models.User
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class InscripcionFragment : Fragment() {

    private var _binding: FragmentInscripcionBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInscripcionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnEnviarInscripcion.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val rol = binding.etRol.text.toString()

            if (username.isBlank() || rol.isBlank()) {
                Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = User(username = username, password = "", rol = rol)
            guardarUsuario(user)
            Toast.makeText(requireContext(), "Inscripción exitosa", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        }
    }

    private fun guardarUsuario(user: User) {
        val prefs = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val current = prefs.getString("inscriptos", null)

        val listType = object : TypeToken<MutableList<User>>() {}.type
        val usuarios = if (current != null) gson.fromJson<MutableList<User>>(current, listType) else mutableListOf()

        usuarios.add(user)
        editor.putString("inscriptos", gson.toJson(usuarios))
        editor.apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
