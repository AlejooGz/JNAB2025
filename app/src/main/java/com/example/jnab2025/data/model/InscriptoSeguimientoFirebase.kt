package com.example.jnab2025.data.model

data class InscriptoSeguimientoFirebase(
    val inscripcion: InscripcionFirebase,
    val comprobante: ComprobanteFirebase? = null
)