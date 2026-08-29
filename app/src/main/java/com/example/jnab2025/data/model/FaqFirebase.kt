package com.example.jnab2025.data.model

data class FaqFirebase(
    val id: String = "",
    val publico: String = PublicoFaq.ASISTENTE.name,
    val pregunta: String = "",
    val respuesta: String = "",
    val orden: Int = 0,
    val publicada: Boolean = true
)