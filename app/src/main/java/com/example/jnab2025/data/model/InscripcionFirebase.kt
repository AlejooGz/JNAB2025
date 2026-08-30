package com.example.jnab2025.data.model

import com.google.firebase.Timestamp

data class InscripcionFirebase(
    val id: String = "",
    val usuarioUid: String = "",
    val usuarioNombre: String = "",
    val usuarioEmail: String = "",
    val tipo: String = TipoInscripcion.ASISTENTE.name,
    val categoria: String = CategoriaInscripcion.GENERAL.name,
    val estado: String = EstadoInscripcion.PENDIENTE_PAGO.name,
    val monto: Double = 0.0,
    val fechaAlta: Timestamp? = null
)