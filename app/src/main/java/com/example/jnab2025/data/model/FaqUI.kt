package com.example.jnab2025.data.model

data class FaqUi(
    val id: String = "",
    val pregunta: String,
    val respuesta: String = "",
    var expandida: Boolean = false,
    val esEncabezado: Boolean = false
)