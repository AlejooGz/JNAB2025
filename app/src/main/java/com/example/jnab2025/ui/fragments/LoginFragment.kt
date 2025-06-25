package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.content.Context
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.data.UserFakeData
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
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val username = sharedPref.getString("username", null)

        // 🔁 Si ya está logueado, redirigí automáticamente
        if (!username.isNullOrEmpty()) {
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
            return
        }

        binding.btnLogin.setOnClickListener {
            val usernameInput = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            val user = UserFakeData.login(usernameInput, password)

            if (user != null) {
                Toast.makeText(requireContext(), "Login exitoso", Toast.LENGTH_SHORT).show()

                sharedPref.edit()
                    .putString("username", user.username)
                    .putString("user_rol", user.rol.lowercase())
                    .putInt("usuario_id", user.id) // 👈 Agregado acá
                    .apply()

                val intent = requireActivity().intent
                requireActivity().finish()
                startActivity(intent)
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