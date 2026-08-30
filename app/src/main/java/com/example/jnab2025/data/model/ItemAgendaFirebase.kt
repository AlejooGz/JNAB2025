package com.example.jnab2025.data.model

import java.time.LocalDate
import java.time.LocalTime

data class ItemAgendaFirebase(
    val charlaId: String,
    val titulo: String,
    val tipo: TipoActividad,
    val fecha: LocalDate,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val aula: String? = null,
    val edificio: String? = null,
    val piso: Int? = null,
    val simposio: String? = null,
    val expositor: String? = null,
    val enMiAgenda: Boolean = false
)