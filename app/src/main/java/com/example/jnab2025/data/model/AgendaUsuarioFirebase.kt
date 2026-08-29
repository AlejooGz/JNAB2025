package com.example.jnab2025.data.model

import com.google.firebase.Timestamp

data class AgendaUsuarioFirebase(
    val usuarioUid: String = "",
    val charlaId: String = "",
    val agregadoEn: Timestamp? = null
)