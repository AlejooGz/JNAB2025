package com.example.jnab2025.data.model
import com.google.firebase.Timestamp


class SimposioFirebase (
    val id: String = "",
    val organizadorUid: String = "",

    val aulaId: String = "",
    val aulaNombre: String = "",
    val aulaEdificio: String = "",
    val aulaPiso: Int = 0,

    val titulo: String = "",
    val descripcion: String = "",
    val temaCentral: String = "",

    val fechaInicio: Timestamp? = null,
    val fechaFin: Timestamp? = null
)