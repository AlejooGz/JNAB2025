package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.R
import com.example.jnab2025.data.firebase.FirebaseSeed
import com.example.jnab2025.data.model.Rol
import com.example.jnab2025.data.model.UsuarioFirebase
import com.example.jnab2025.databinding.FragmentLoginBinding
import com.example.jnab2025.utils.Sesion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(
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

        if (Sesion.haySesion(requireContext())) {

            findNavController().navigate(
                R.id.action_loginFragment_to_mainFragment
            )
            return
        }
        binding.btnLogin.setOnClickListener {
            intentarLogin()
        }
        binding.tvRegistrarse.setOnClickListener {
            findNavController().navigate(
                R.id.action_loginFragment_to_registroFragment
            )
        }
    }
    private fun intentarLogin() {
        val email = binding.etUsername.text
            .toString()
            .trim()
            .lowercase()
        val password = binding.etPassword.text
            .toString()
        if (email.isBlank() || password.isBlank()) {
            avisar("Completá el email y la contraseña")
            return
        }
        binding.btnLogin.isEnabled = false

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { resultado ->

                val uid = resultado.user?.uid

                if (uid == null) {
                    binding.btnLogin.isEnabled = true
                    avisar(
                        "No se pudo obtener el usuario"
                    )
                    return@addOnSuccessListener
                }
                cargarPerfil(uid)
            }
            .addOnFailureListener {
                binding.btnLogin.isEnabled = true
                avisar(
                    "Email o contraseña incorrectos"
                )
            }
    }
    private fun cargarPerfil(uid: String) {
        firestore
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { documento ->
                if (!documento.exists()) {
                    binding.btnLogin.isEnabled = true
                    auth.signOut()
                    avisar(
                        "No se encontró el perfil del usuario"
                    )
                    return@addOnSuccessListener
                }

                val usuario =
                    documento.toObject(
                        UsuarioFirebase::class.java
                    )
                if (usuario == null) {
                    binding.btnLogin.isEnabled = true
                    auth.signOut()
                    avisar(
                        "Error al leer los datos del usuario"
                    )
                    return@addOnSuccessListener
                }
                val rol = try {
                    Rol.valueOf(
                        usuario.rol.uppercase()
                    )
                } catch (e: Exception) {
                    binding.btnLogin.isEnabled = true
                    auth.signOut()
                    avisar(
                        "El rol del usuario no es válido"
                    )
                    return@addOnSuccessListener
                }
                Sesion.iniciarFirebase(
                    context = requireContext(),
                    uid = uid,
                    nombre = usuario.nombreCompleto,
                    email = usuario.email,
                    roles = setOf(rol)
                )
                sembrarAulasSiHaceFalta()
                avisar(
                    "Bienvenido, ${usuario.nombreCompleto}"
                )
                findNavController().navigate(
                    R.id.action_loginFragment_to_mainFragment
                )
            }
            .addOnFailureListener { error ->
                binding.btnLogin.isEnabled = true
                auth.signOut()
                avisar(
                    "Error al cargar el perfil: ${error.message}"
                )
            }
    }
    /**
     * Crea las aulas iniciales la primera vez que alguien entra. Va aca y no en
     * MainActivity porque necesita sesion iniciada: escribir en Firestore sin
     * usuario autenticado falla si las reglas piden auth.
     */
    private fun sembrarAulasSiHaceFalta() {
        FirebaseSeed.cargarAulasSiFaltan(
            onListo = { sembro ->
                if (sembro) Log.d("FirebaseSeed", "Aulas iniciales creadas")
                else Log.d("FirebaseSeed", "Las aulas ya estaban cargadas")
            },
            onError = { mensaje ->
                Log.e("FirebaseSeed", "No se pudieron cargar las aulas: $mensaje")
            }
        )
    }

    private fun avisar(mensaje: String) {
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