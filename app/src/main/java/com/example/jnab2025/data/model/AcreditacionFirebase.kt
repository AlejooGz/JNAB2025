package com.example.jnab2025.data.model

import com.google.firebase.Timestamp

/**
 * Ingreso de un inscripto al evento en un dia (coleccion "acreditaciones").
 *
 * El id del documento es "{dia}_{uid}": hay un solo registro por persona y por
 * dia. Asi cada jornada arranca sin nadie acreditado (el dia siguiente busca
 * otro id) y dos escaneos del mismo dia apuntan al mismo documento.
 */
data class AcreditacionFirebase(
    val id: String = "",
    val uid: String = "",
    /** yyyy-MM-dd, en la zona horaria del evento. */
    val dia: String = "",
    val usuarioNombre: String = "",
    val tipo: String = "",
    /** Hora del escaneo segun el celular del organizador (vale aunque se suba despues). */
    val fechaHora: Timestamp? = null,
    val acreditadoPorUid: String = "",
    val acreditadoPorNombre: String = ""
)
