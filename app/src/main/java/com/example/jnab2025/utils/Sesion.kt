package com.example.jnab2025.utils

import android.content.Context
import com.example.jnab2025.data.model.Rol
import com.google.firebase.auth.FirebaseAuth

/**
 * Sesion del usuario logueado. Guarda lo minimo que la UI necesita sin volver a
 * consultar Firestore: el uid, el nombre, el email y los roles.
 *
 * La fuente de verdad de la autenticacion es FirebaseAuth; esto es solo una
 * copia local para armar el menu y los saludos.
 */
object Sesion {

    private const val PREFS = "JnabSesion"
    private const val K_FIREBASE_UID = "firebaseUid"
    private const val K_NOMBRE = "nombre"
    private const val K_EMAIL = "email"
    private const val K_ROLES = "roles"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun iniciarFirebase(
        context: Context,
        uid: String,
        nombre: String,
        email: String,
        roles: Set<Rol>
    ) {
        prefs(context).edit()
            .putString(K_FIREBASE_UID, uid)
            .putString(K_NOMBRE, nombre)
            .putString(K_EMAIL, email)
            .putStringSet(K_ROLES, roles.map { it.name }.toSet())
            .apply()
    }

    fun cerrar(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun firebaseUid(context: Context): String? =
        prefs(context).getString(K_FIREBASE_UID, null)

    fun nombre(context: Context): String =
        prefs(context).getString(K_NOMBRE, "Invitado") ?: "Invitado"

    fun email(context: Context): String? = prefs(context).getString(K_EMAIL, null)

    fun roles(context: Context): Set<Rol> =
        prefs(context).getStringSet(K_ROLES, emptySet())
            .orEmpty()
            .mapNotNull { nombre -> runCatching { Rol.valueOf(nombre) }.getOrNull() }
            .toSet()

    /**
     * Hay sesion solo si la copia local coincide con la de FirebaseAuth, que es
     * la fuente de verdad.
     *
     * Si el token se cayo (cuenta borrada, contrasenia cambiada desde otro
     * lado) las preferencias quedaban diciendo que habia sesion: el login te
     * mandaba directo a la app y todas las pantallas aparecian vacias, porque
     * los ViewModels leen auth.currentUser. Ante esa desincronizacion se limpia
     * la copia local y se vuelve al login.
     */
    fun haySesion(context: Context): Boolean {
        val guardado = firebaseUid(context) ?: return false
        val actual = FirebaseAuth.getInstance().currentUser?.uid

        if (guardado != actual) {
            cerrar(context)
            return false
        }
        return true
    }

    fun tieneRol(context: Context, rol: Rol): Boolean = rol in roles(context)

    fun esExpositor(context: Context): Boolean = tieneRol(context, Rol.EXPOSITOR)
    fun esOrganizador(context: Context): Boolean = tieneRol(context, Rol.ORGANIZADOR)
    fun esAsistente(context: Context): Boolean = tieneRol(context, Rol.ASISTENTE)
}
