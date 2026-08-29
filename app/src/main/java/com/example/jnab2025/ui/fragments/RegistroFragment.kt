package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentRegistroBinding
import com.example.jnab2025.data.model.UsuarioFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegistroFragment : Fragment() {
    private var _binding: FragmentRegistroBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegistroBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        configurarRoles()

        binding.btnRegistrarse.setOnClickListener {
            registrarUsuario()
        }

        binding.tvIrLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    private fun configurarRoles() {
        val roles = listOf(
            "ASISTENTE",
            "EXPOSITOR"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            roles
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spRol.adapter = adapter
    }

    private fun registrarUsuario() {
        val nombre = binding.etNombreCompleto.text
            .toString()
            .trim()
        val email = binding.etEmailRegistro.text
            .toString()
            .trim()
            .lowercase()
        val password = binding.etPasswordRegistro.text
            .toString()
        val confirmarPassword = binding.etConfirmarPassword.text
            .toString()
        val rol = binding.spRol.selectedItem.toString()

        if (nombre.isBlank()) {
            mostrarMensaje("Ingresá tu nombre completo")
            return
        }
        if (email.isBlank()) {
            mostrarMensaje("Ingresá tu email")
            return
        }

        if (password.isBlank()) {
            mostrarMensaje("Ingresá una contraseña")
            return
        }

        if (password.length < 6) {
            mostrarMensaje(
                "La contraseña debe tener al menos 6 caracteres"
            )
            return
        }

        if (password != confirmarPassword) {
            mostrarMensaje(
                "Las contraseñas no coinciden"
            )
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { resultado ->

                val uid = resultado.user?.uid

                if (uid == null) {
                    mostrarMensaje("No se pudo obtener el identificador del usuario")
                    return@addOnSuccessListener
                }

                val usuario = UsuarioFirebase(
                    uid = uid,
                    nombreCompleto = nombre,
                    email = email,
                    rol = rol
                )
                firestore
                    .collection("users")
                    .document(uid)
                    .set(usuario)
                    .addOnSuccessListener {

                        mostrarMensaje(
                            "Usuario registrado correctamente"
                        )

                        auth.signOut()

                        findNavController().navigateUp()
                    }
                    .addOnFailureListener { error ->

                        mostrarMensaje(
                            "Error al guardar el perfil: ${error.message}"
                        )
                    }
            }
            .addOnFailureListener { error ->

                mostrarMensaje(
                    "Error al registrar usuario: ${error.message}"
                )
            }
    }
    private fun mostrarMensaje(mensaje: String) {
        Toast.makeText(
            requireContext(),
            mensaje,
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}