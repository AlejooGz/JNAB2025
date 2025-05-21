package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.content.Context
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.data.UserRepository
import com.example.jnab2025.databinding.FragmentLoginBinding
import com.example.jnab2025.R


/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if (UserRepository.login(username, password)) {
                Toast.makeText(requireContext(), "Login exitoso", Toast.LENGTH_SHORT).show()
                val rol = when (username) {
                    "usuario1" -> "expositor"
                    "usuario2" -> "asistente"
                    "admin" -> "organizador"
                    else -> "invitado"
                }

                val sharedPref = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                // sharedPref.edit().putString("user_rol", rol).apply()
                sharedPref.edit()
                    .putString("username", username)
                    .putString("user_rol", rol)
                    .apply()

                // Navegar a otro fragmento (ajustar según tu navigation graph)
                findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
            } else {
                Toast.makeText(requireContext(), "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}