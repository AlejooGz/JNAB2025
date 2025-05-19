package com.example.jnab2025.models


data class Tramite(
    val id: Int,
    val tituloTrabajo: String,
    var estado: String = "Pendiente",
    var pagado: Boolean = false,
    val fechaEnvio: String = "18/05/2025",
    val nombreArchivo: String = "Sin archivo"
)

