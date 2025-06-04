package com.example.jnab2025.utils

import android.content.Context

object SesionUsuario {
    fun obtenerRol(context: Context): String {
        val sharedPref = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPref.getString("user_rol", "invitado")?.lowercase() ?: "invitado"
    }

    fun esExpositor(context: Context): Boolean = obtenerRol(context) == "expositor"
    fun esOrganizador(context: Context): Boolean = obtenerRol(context) == "organizador"
    fun esAsistente(context: Context): Boolean = obtenerRol(context) == "asistente"
}