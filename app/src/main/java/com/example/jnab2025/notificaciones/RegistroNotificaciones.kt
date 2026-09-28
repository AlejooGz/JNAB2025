package com.example.jnab2025.notificaciones

import android.content.Context

/**
 * Memoria local de lo que este dispositivo ya hizo.
 *
 * Hace falta porque el listener en vivo y el worker de segundo plano leen la
 * misma coleccion: sin este registro, una notificacion aprobada saldria
 * repetida en cada arranque de la app. Es el filtro rapido; el definitivo es
 * el campo `notificada` del doc en Firestore.
 *
 * Los ids ya mostrados NO se borran al cerrar sesion: cada doc de
 * `notificaciones` tiene un unico destinatario, asi que recordar ids de otro
 * usuario no le quita avisos a nadie, y borrarlos hacia que todo lo ya
 * avisado volviera a sonar en el siguiente login.
 */
object RegistroNotificaciones {

    private const val PREFS = "JnabNotificaciones"
    /** Formato viejo (Set sin orden); se migra a [K_MOSTRADAS_ORDEN]. */
    private const val K_MOSTRADAS = "mostradas"
    /** Ids separados por salto de linea, del mas viejo al mas nuevo. */
    private const val K_MOSTRADAS_ORDEN = "mostradasOrden"
    private const val K_PROGRAMADAS = "charlasProgramadas"
    /** Prefijo + UID: desde cuando este organizador vigila comprobantes. */
    private const val K_LINEA_BASE = "lineaBaseComprobantes_"
    /** Ids de aviso de comprobante que este dispositivo ya dejo en Firestore. */
    private const val K_COMPROBANTES_DETECTADOS = "comprobantesDetectados"

    /** Tope para que la lista de ids ya avisados no crezca sin control. */
    private const val MAX_MOSTRADAS = 200

    private fun prefs(context: Context) =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    // --- Notificaciones de Firestore ya mostradas -------------------------

    private fun mostradas(context: Context): List<String> {
        val prefs = prefs(context)
        val ordenadas =
            prefs.getString(K_MOSTRADAS_ORDEN, null)
                ?.split('\n')
                ?.filter { it.isNotBlank() }
                .orEmpty()
        // los del formato viejo van primero: son anteriores a la migracion
        val viejas =
            prefs.getStringSet(K_MOSTRADAS, emptySet())
                .orEmpty()
                .filter { it !in ordenadas }
        return viejas + ordenadas
    }

    fun yaMostrada(context: Context, notificacionId: String): Boolean =
        notificacionId in mostradas(context)

    fun marcarMostrada(context: Context, notificacionId: String) {
        val actuales = mostradas(context) - notificacionId + notificacionId

        // al pasar el tope se olvidan los mas viejos, que ya nadie va a volver
        // a emitir (y si llegara a pasar, el campo `notificada` los frena)
        val podadas = actuales.takeLast(MAX_MOSTRADAS)

        prefs(context).edit()
            .putString(K_MOSTRADAS_ORDEN, podadas.joinToString("\n"))
            .remove(K_MOSTRADAS)
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

    // --- Comprobantes recibidos (organizador) -----------------------------

    /**
     * Momento (epoch millis) desde el que este organizador recibe avisos de
     * comprobantes en este dispositivo. La primera vez se fija en "ahora":
     * los que ya estaban pendientes no suenan (siguen en Ver inscriptos).
     */
    fun lineaBaseComprobantes(context: Context, organizadorUid: String): Long {
        val clave = K_LINEA_BASE + organizadorUid
        val prefs = prefs(context)
        val guardada = prefs.getLong(clave, 0L)
        if (guardada > 0L) return guardada

        val ahora = System.currentTimeMillis()
        prefs.edit().putLong(clave, ahora).apply()
        return ahora
    }

    fun comprobanteDetectado(context: Context, idAviso: String): Boolean =
        idAviso in comprobantesDetectados(context)

    fun marcarComprobanteDetectado(context: Context, idAviso: String) {
        val actuales = comprobantesDetectados(context) - idAviso + idAviso
        prefs(context).edit()
            .putString(
                K_COMPROBANTES_DETECTADOS,
                actuales.takeLast(MAX_MOSTRADAS).joinToString("\n")
            )
            .apply()
    }

    private fun comprobantesDetectados(context: Context): List<String> =
        prefs(context)
            .getString(K_COMPROBANTES_DETECTADOS, null)
            ?.split('\n')
            ?.filter { it.isNotBlank() }
            .orEmpty()
}
