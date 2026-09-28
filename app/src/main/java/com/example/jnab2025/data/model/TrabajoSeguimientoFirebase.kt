package com.example.jnab2025.data.model

import java.time.LocalDate
import java.time.LocalTime

class TrabajoSeguimientoFirebase(
    val trabajo: TrabajoFirebase,
    val fecha: LocalDate? = null,
    val horaInicio: LocalTime? = null,
    val horaFin: LocalTime? = null,
    val aula: String? = null
)
