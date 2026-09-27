package com.example.jnab2025.data.model

import com.google.firebase.Timestamp

/**
 * Actividad general del evento.
 *
 * A diferencia de CharlaFirebase, una actividad no pertenece
 * necesariamente a un simposio ni a un trabajo.
 *
 * Ejemplos:
 * - Conferencia
 * - Coffee break
 * - Acreditación
 * - Almuerzo
 * - Otra actividad
 */
data class ActividadFirebase(
    val id: String = "",
    val eventoId: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: String = TipoActividad.OTRO.name,
    val fecha: Timestamp? = null,
    val aulaId: String = "",
    val aulaNombre: String = "",
    val aulaEdificio: String = "",
    val aulaPiso: Int = 0,
    val horaInicio: String = "",
    val horaFin: String = ""
)