package com.example.jnab2025.ui.fragments

import android.content.DialogInterface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.R
import com.example.jnab2025.data.model.Rol
import com.example.jnab2025.data.model.UsuarioFirebase
import com.example.jnab2025.databinding.FragmentLoginBinding
import com.example.jnab2025.utils.Sesion
import com.example.jnab2025.utils.mostrarCargando
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var credentialManager: CredentialManager

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
        super.onViewCreated(
            view,
            savedInstanceState
        )

        credentialManager =
            CredentialManager.create(
                requireContext()
            )

        // Si ya existe una sesión local,
        // no mostramos nuevamente el login.
        if (Sesion.haySesion(requireContext())) {

            findNavController().navigate(
                R.id.action_loginFragment_to_mainFragment
            )

            return
        }

        binding.btnLogin.setOnClickListener {
            intentarLogin()
        }

        binding.btnGoogle.setOnClickListener {
            iniciarConGoogle()
        }

        binding.tvRegistrarse.setOnClickListener {

            findNavController().navigate(
                R.id.action_loginFragment_to_registroFragment
            )
        }
    }

    // ---------------------------------------------------------
    // login con google
    // ---------------------------------------------------------

    private fun iniciarConGoogle() {

        deshabilitarBotones()

        val opcionGoogle =
            GetSignInWithGoogleOption
                .Builder(
                    getString(
                        R.string.default_web_client_id
                    )
                )
                .build()

        val solicitud =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(
                    opcionGoogle
                )
                .build()

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val resultado =
                    credentialManager.getCredential(
                        context = requireContext(),
                        request = solicitud
                    )

                procesarCredencialGoogle(
                    resultado
                )

            } catch (error: GetCredentialException) {

                habilitarBotones()

                avisar(
                    "No se completó el acceso con Google"
                )
            }
        }
    }

    private fun procesarCredencialGoogle(
        resultado: GetCredentialResponse
    ) {

        val credencial =
            resultado.credential

        if (
            credencial !is CustomCredential ||
            credencial.type !=
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {

            habilitarBotones()

            avisar(
                "La credencial recibida no es válida"
            )

            return
        }

        try {

            val credencialGoogle =
                GoogleIdTokenCredential.createFrom(
                    credencial.data
                )

            autenticarEnFirebase(
                credencialGoogle.idToken
            )

        } catch (
            error: GoogleIdTokenParsingException
        ) {

            habilitarBotones()

            avisar(
                "No se pudo interpretar la cuenta de Google"
            )
        }
    }

    private fun autenticarEnFirebase(
        idToken: String
    ) {

        // recién acá empieza la espera contra Firebase; antes el usuario
        // estaba eligiendo la cuenta en el selector de Google
        mostrarCargando(
            true,
            "Ingresando…"
        )

        val credencialFirebase =
            GoogleAuthProvider.getCredential(
                idToken,
                null
            )

        auth.signInWithCredential(
            credencialFirebase
        )
            .addOnSuccessListener { resultado ->

                val usuarioFirebase =
                    resultado.user

                if (usuarioFirebase == null) {

                    habilitarBotones()

                    avisar(
                        "No se pudo obtener el usuario de Google"
                    )

                    return@addOnSuccessListener
                }

                buscarPerfilGoogle(
                    usuarioFirebase
                )
            }

            .addOnFailureListener { error ->

                habilitarBotones()

                if (
                    error is FirebaseAuthUserCollisionException
                ) {

                    avisar(
                        "Ya existe una cuenta con ese email. " +
                                "Ingresá con tu contraseña para vincular Google."
                    )

                } else {

                    avisar(
                        "No se pudo iniciar sesión con Google: " +
                                error.localizedMessage
                    )
                }
            }
    }

    /**
     * Si users/{uid} existe, el usuario ya había ingresado.
     * Si no existe, es su primer ingreso con Google.
     */
    private fun buscarPerfilGoogle(
        usuarioFirebase: FirebaseUser
    ) {

        firestore
            .collection("users")
            .document(
                usuarioFirebase.uid
            )
            .get()
            .addOnSuccessListener { documento ->

                if (documento.exists()) {

                    cargarPerfil(
                        usuarioFirebase.uid
                    )

                } else {

                    pedirRolParaUsuarioGoogle(
                        usuarioFirebase
                    )
                }
            }
            .addOnFailureListener { error ->

                habilitarBotones()

                auth.signOut()

                avisar(
                    "No se pudo consultar el perfil: " +
                            error.localizedMessage
                )
            }
    }

    /**
     * Google conoce nombre y correo, pero no sabe
     * qué rol tiene la persona dentro de JNAB.
     */
    private fun pedirRolParaUsuarioGoogle(
        usuarioFirebase: FirebaseUser
    ) {

        // el usuario tiene que elegir el rol: no hay nada cargando
        mostrarCargando(false)

        val opciones =
            arrayOf(
                "Asistente",
                "Expositor"
            )

        var posicionSeleccionada = 0

        val dialogo =
            MaterialAlertDialogBuilder(
                requireContext(),
                R.style.ThemeOverlay_Jnab_Dialog
            )
                .setTitle(
                    "¿Cómo vas a participar?"
                )
                .setSingleChoiceItems(
                    opciones,
                    posicionSeleccionada
                ) { _, posicion ->

                    posicionSeleccionada =
                        posicion
                }
                .setPositiveButton(
                    "Continuar"
                ) { _, _ ->

                    val rol =
                        if (
                            posicionSeleccionada == 1
                        ) {

                            "EXPOSITOR"

                        } else {

                            "ASISTENTE"
                        }

                    guardarPerfilGoogle(
                        usuarioFirebase =
                            usuarioFirebase,
                        rol =
                            rol
                    )
                }
                .setNegativeButton(
                    "Cancelar"
                ) { _, _ ->

                    auth.signOut()

                    habilitarBotones()
                }
                .setOnCancelListener {

                    auth.signOut()

                    habilitarBotones()
                }
                .create()

        dialogo.setOnShowListener {

            val radioEsquinas =
                28 *
                        resources
                            .displayMetrics
                            .density

            val fondoCream =
                GradientDrawable().apply {

                    shape =
                        GradientDrawable.RECTANGLE

                    setColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.cream
                        )
                    )

                    cornerRadius =
                        radioEsquinas
                }

            dialogo
                .window
                ?.setBackgroundDrawable(
                    fondoCream
                )

            dialogo.getButton(
                DialogInterface.BUTTON_POSITIVE
            ).setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dark_red
                )
            )

            dialogo.getButton(
                DialogInterface.BUTTON_NEGATIVE
            ).setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dark_red
                )
            )
        }

        dialogo.show()
    }

    /**
     * Crea el perfil de Firestore utilizando
     * los datos entregados por Google.
     */
    private fun guardarPerfilGoogle(
        usuarioFirebase: FirebaseUser,
        rol: String
    ) {

        val email =
            usuarioFirebase
                .email
                ?.trim()
                ?.lowercase()
                .orEmpty()

        val nombre =
            usuarioFirebase
                .displayName
                ?.trim()
                ?.takeIf {
                    it.isNotBlank()
                }
                ?: email
                    .substringBefore("@")
                    .replaceFirstChar {
                        it.uppercase()
                    }

        if (email.isBlank()) {

            habilitarBotones()

            auth.signOut()

            avisar(
                "Google no proporcionó un correo electrónico"
            )

            return
        }

        val usuario =
            UsuarioFirebase(
                uid =
                    usuarioFirebase.uid,
                nombreCompleto =
                    nombre,
                email =
                    email,
                rol =
                    rol
            )

        mostrarCargando(
            true,
            "Creando tu perfil…"
        )

        firestore
            .collection("users")
            .document(
                usuarioFirebase.uid
            )
            .set(
                usuario
            )
            .addOnSuccessListener {

                iniciarSesionLocal(
                    usuario
                )
            }
            .addOnFailureListener { error ->

                habilitarBotones()

                auth.signOut()

                avisar(
                    "La cuenta se autenticó, pero no se pudo " +
                            "guardar el perfil: ${error.localizedMessage}"
                )
            }
    }

    // ---------------------------------------------------------
    // login normal con email y contraseña
    // ---------------------------------------------------------

    private fun intentarLogin() {

        val email =
            binding
                .etUsername
                .text
                .toString()
                .trim()
                .lowercase()

        val password =
            binding
                .etPassword
                .text
                .toString()

        if (
            email.isBlank() ||
            password.isBlank()
        ) {

            avisar(
                "Completá el email y la contraseña"
            )

            return
        }

        deshabilitarBotones()

        mostrarCargando(
            true,
            "Ingresando…"
        )

        auth.signInWithEmailAndPassword(
            email,
            password
        )
            .addOnSuccessListener { resultado ->

                val uid =
                    resultado.user?.uid

                if (uid == null) {

                    habilitarBotones()

                    avisar(
                        "No se pudo obtener el usuario"
                    )

                    return@addOnSuccessListener
                }

                cargarPerfil(
                    uid
                )
            }
            .addOnFailureListener {

                habilitarBotones()

                avisar(
                    "Email o contraseña incorrectos"
                )
            }
    }

    // ---------------------------------------------------------
    // perfil del usuario
    // ---------------------------------------------------------

    private fun cargarPerfil(
        uid: String
    ) {

        firestore
            .collection("users")
            .document(
                uid
            )
            .get()
            .addOnSuccessListener { documento ->

                if (
                    !documento.exists()
                ) {

                    habilitarBotones()

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

                    habilitarBotones()

                    auth.signOut()

                    avisar(
                        "Error al leer los datos del usuario"
                    )

                    return@addOnSuccessListener
                }

                iniciarSesionLocal(
                    usuario
                )
            }
            .addOnFailureListener { error ->

                habilitarBotones()

                auth.signOut()

                avisar(
                    "Error al cargar el perfil: " +
                            error.localizedMessage
                )
            }
    }

    /**
     * Convierte el rol guardado en Firestore al enum Rol,
     * guarda la sesión local y entra a la pantalla principal.
     */
    private fun iniciarSesionLocal(
        usuario: UsuarioFirebase
    ) {

        val rol =
            try {

                Rol.valueOf(
                    usuario
                        .rol
                        .uppercase()
                )

            } catch (
                error: Exception
            ) {

                habilitarBotones()

                auth.signOut()

                avisar(
                    "El rol del usuario no es válido"
                )

                return
            }

        Sesion.iniciarFirebase(
            context =
                requireContext(),
            uid =
                usuario.uid,
            nombre =
                usuario.nombreCompleto,
            email =
                usuario.email,
            roles =
                setOf(rol)
        )

        avisar(
            "Bienvenido, ${usuario.nombreCompleto}"
        )

        findNavController().navigate(
            R.id.action_loginFragment_to_mainFragment
        )
    }

    private fun deshabilitarBotones() {

        binding.btnLogin.isEnabled =
            false

        binding.btnGoogle.isEnabled =
            false

        binding.tvRegistrarse.isEnabled =
            false
    }

    private fun habilitarBotones() {

        // todos los caminos de error pasan por acá: se apaga la ruedita
        mostrarCargando(false)

        if (_binding == null) {
            return
        }

        binding.btnLogin.isEnabled =
            true

        binding.btnGoogle.isEnabled =
            true

        binding.tvRegistrarse.isEnabled =
            true
    }

    private fun avisar(
        mensaje: String
    ) {

        Toast.makeText(
            requireContext(),
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {

        super.onDestroyView()

        // al entrar bien se navega con la ruedita prendida; se apaga acá
        mostrarCargando(false)

        _binding = null
    }
}