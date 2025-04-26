package com.example.jnab2025

import java.time.LocalTime

data class Charla(
    val id: Int,
    val titulo: String,
    val expositor: String,
    var fecha: String, // Formato "yyyy-MM-dd"
    var horaInicio: String, // Formato "HH:mm"
    var horaFin: String, // Formato "HH:mm"
    var sala: String,
    var esFavorito: Boolean = false,
    val esDestacado: Boolean = false
)