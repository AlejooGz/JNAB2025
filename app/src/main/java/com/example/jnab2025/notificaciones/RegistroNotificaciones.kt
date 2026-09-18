package com.example.jnab2025.notificaciones

import android.content.Context

/**
 * Memoria local de lo que este dispositivo ya hizo.
 *
 * Hace falta porque el mismo documento de Firestore lo pueden ver varios
 * dispositivos del mismo usuario, y porque el listener en vivo y el worker de
 * segundo plano leen la misma coleccion: sin este registro, una notificacion
 * aprobada saldria repetida en cada arranque de la app.
 */
object RegistroNotificaciones {

    private const val PREFS = "JnabNotificaciones"
    private const val K_MOSTRADAS = "mostradas"
    private const val K_PROGRAMADAS = "charlasProgramadas"

    /** Tope para que el set de ids ya avisados no crezca sin control. */
    private const val MAX_MOSTRADAS = 200

    private fun prefs(context: Context) =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // --- Notificaciones de Firestore ya mostradas -------------------------

    fun yaMostrada(context: Context, notificacionId: String): Boolean =
        notificacionId in prefs(context)
            .getStringSet(K_MOSTRADAS, emptySet())
            .orEmpty()

    fun marcarMostrada(context: Context, notificacionId: String) {
        val actuales =
            prefs(context)
                .getStringSet(K_MOSTRADAS, emptySet())
                .orEmpty()
                .toMutableSet()

        actuales += notificacionId

        // getStringSet no conserva orden, asi que al podar se descarta
        // cualquier subconjunto. Es aceptable: lo unico que se pierde es la
        // memoria de avisos viejos, que ya nadie va a volver a emitir.
        val podadas =
            if (actuales.size > MAX_MOSTRADAS) {
                actuales.take(MAX_MOSTRADAS / 2).toSet()
            } else {
                actuales
            }

        prefs(context).edit()
            .putStringSet(K_MOSTRADAS, podadas)
            .apply()
    }

    // --- Recordatorios de charla programados ------------------------------

    /** Ids de charla con alarma viva, para poder cancelarlas despues. */
    fun charlasProgramadas(context: Context): Set<String> =
        prefs(context)
            .getStringSet(K_PROGRAMADAS, emptySet())
            .orEmpty()

    fun guardarProgramadas(context: Context, charlaIds: Set<String>) {
        prefs(context).edit()
            .putStringSet(K_PROGRAMADAS, charlaIds)
            .apply()
    }

    /** Al cerrar sesion no deben quedar avisos del usuario anterior. */
    fun limpiar(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
