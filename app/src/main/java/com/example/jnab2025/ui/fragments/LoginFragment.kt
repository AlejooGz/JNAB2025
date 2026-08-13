package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentLoginBinding
import com.example.jnab2025.ui.viewmodels.LoginViewModel
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si ya hay sesion abierta, entra directo.
        if (Sesion.haySesion(requireContext())) {
            findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
            return
        }

        binding.btnLogin.setOnClickListener { intentarLogin() }
    }

    private fun intentarLogin() {
        val email = binding.etUsername.text.toString()
        val password = binding.etPassword.text.toString()

        if (email.isBlank() || password.isBlank()) {
            avisar("Completa el email y la contrasenia")
            return
        }

        binding.btnLogin.isEnabled = false
        viewLifecycleOwner.lifecycleScope.launch {
            when (val resultado = viewModel.login(email, password)) {
                is LoginViewModel.Resultado.Ok -> {
                    Sesion.iniciar(
                        requireContext(),
                        resultado.usuario,
                        resultado.roles,
                        resultado.eventoId
                    )
                    avisar("Bienvenido, ${resultado.usuario.nombre}")
                    findNavController().navigate(R.id.action_loginFragment_to_mainFragment)
                }

                LoginViewModel.Resultado.CredencialesInvalidas -> {
                    binding.btnLogin.isEnabled = true
                    avisar("Email o contrasenia incorrectos")
                }

                LoginViewModel.Resultado.SinEvento -> {
                    binding.btnLogin.isEnabled = true
                    avisar("La base se esta preparando, intenta de nuevo en un segundo")
                }
            }
        }
    }

    private fun avisar(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
