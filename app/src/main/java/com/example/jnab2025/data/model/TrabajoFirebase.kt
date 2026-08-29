package com.example.jnab2025.data.model
import com.google.firebase.Timestamp

data class TrabajoFirebase (
    val id: String = "",
    val simposioId: String = "",
    val simposioTitulo: String = "",
    val autorUid: String = "",
    val autorNombre: String = "",
    val autorEmail: String = "",
    val titulo: String = "",
    val resumen: String = "",
    val archivoUrl: String = "",
    val nombreArchivo: String = "",
    val fechaEnvio: Timestamp? = null,
    val estado: String = "ENVIADO",
    val motivoRechazo: String? = null,
    val fechaResolucion: Timestamp? = null,
    val resueltoPorUid: String? = null
)