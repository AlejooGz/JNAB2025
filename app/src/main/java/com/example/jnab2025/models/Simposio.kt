package com.example.jnab2025.models

data class Simposio (
    val id: Int,
    val titulo: String,
    var fechaInicio: String, // Formato "yyyy-MM-dd"
    var fechaFin: String, // Formato "yyyy-MM-dd"
)