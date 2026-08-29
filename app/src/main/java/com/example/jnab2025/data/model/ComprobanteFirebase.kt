package com.example.jnab2025.data.model

import com.google.firebase.Timestamp

data class ComprobanteFirebase (
    val id: String = "",
    val inscripcionId: String = "",
    val usuarioUid: String = "",
    val archivoUrl: String = "",
    val nombreArchivo: String = "",
    val fechaCarga: Timestamp? = null,
    val estado: String = "PENDIENTE",
    val verificadoPorUid: String? = null,
    val fechaVerificacion: Timestamp? = null,
    val motivoRechazo: String? = null
)