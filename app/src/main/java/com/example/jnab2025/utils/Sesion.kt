package com.example.jnab2025.utils

import android.content.Context
import com.example.jnab2025.data.model.Rol
import com.example.jnab2025.data.model.Usuario

/**
 * Sesion del usuario logueado. Reemplaza a [SesionUsuario], que guardaba el rol
 * como texto libre y el id como Int.
 *
 * Usa un archivo de preferencias propio ("JnabSesion") para no pisarse con el
 * "AppPreferences" que todavia usan las pantallas sin migrar.
 */
object Sesion {

    private const val PREFS = "JnabSesion"
    private const val K_USUARIO = "usuarioId"
    private const val K_EVENTO = "eventoId"
    private const val K_NOMBRE = "nombre"
    private const val K_EMAIL = "email"
    private const val K_ROLES = "roles"

    const val SIN_SESION = -1L

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun iniciar(context: Context, usuario: Usuario, roles: Set<Rol>, eventoId: Long) {
        prefs(context).edit()
            .putLong(K_USUARIO, usuario.id)
            .putLong(K_EVENTO, eventoId)
            .putString(K_NOMBRE, usuario.nombreCompleto)
            .putString(K_EMAIL, usuario.email)
            .putStringSet(K_ROLES, roles.map { it.name }.toSet())
            .apply()
    }

    fun cerrar(context: Context) {
        prefs(context).edit().clear().apply()
    }

    fun usuarioId(context: Context): Long = prefs(context).getLong(K_USUARIO, SIN_SESION)

    fun eventoId(context: Context): Long = prefs(context).getLong(K_EVENTO, SIN_SESION)

    fun nombre(context: Context): String = prefs(context).getString(K_NOMBRE, "Invitado") ?: "Invitado"

    fun email(context: Context): String? = prefs(context).getString(K_EMAIL, null)

    fun roles(context: Context): Set<Rol> =
        prefs(context).getStringSet(K_ROLES, emptySet())
            .orEmpty()
            .mapNotNull { nombre -> runCatching { Rol.valueOf(nombre) }.getOrNull() }
            .toSet()

    fun haySesion(context: Context): Boolean = usuarioId(context) != SIN_SESION

    fun tieneRol(context: Context, rol: Rol): Boolean = rol in roles(context)

    fun esExpositor(context: Context): Boolean = tieneRol(context, Rol.EXPOSITOR)
    fun esOrganizador(context: Context): Boolean = tieneRol(context, Rol.ORGANIZADOR)
    fun esAsistente(context: Context): Boolean = tieneRol(context, Rol.ASISTENTE)
}
