package com.example.jnab2025.data.model

import java.time.LocalDate
import java.time.LocalTime

data class ItemAgendaFirebase(
    val charlaId: String = "",
    val actividadId: String = "",
    val titulo: String = "",
    val tipo: TipoActividad = TipoActividad.OTRO,
    val fecha: LocalDate = LocalDate.MIN,
    val horaInicio: LocalTime = LocalTime.MIN,
    val horaFin: LocalTime = LocalTime.MIN,
    val aula: String? = null,
    val edificio: String? = null,
    val piso: Int? = null,
    val simposio: String? = null,
    val expositor: String? = null,
    val enMiAgenda: Boolean = false
) {
    val id: String
        get() = if (charlaId.isNotBlank()) charlaId else actividadId

    val esActividad: Boolean
        get() = actividadId.isNotBlank()
}