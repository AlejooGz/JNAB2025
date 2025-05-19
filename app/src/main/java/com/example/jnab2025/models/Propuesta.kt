package com.example.jnab2025.models

data class Propuesta(
    val id: Int,
    val simposioId: Int,
    val titulo: String,
    val expositor: String,
    val descripcion: String,
    val estado: EstadoPropuesta
)

enum class EstadoPropuesta {
    PENDIENTE,
    APROBADA,
    RECHAZADA
}