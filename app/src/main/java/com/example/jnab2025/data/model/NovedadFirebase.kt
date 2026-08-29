package com.example.jnab2025.data.model

import com.google.firebase.Timestamp

data class NovedadFirebase (
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val imagenUrl: String? = null,
    val fechaPublicacion: Timestamp? = null,
    val autorUid: String = "",
    val publicada: Boolean = true
)