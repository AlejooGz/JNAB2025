package com.example.jnab2025.data.model
import com.google.firebase.Timestamp

data class CharlaFirebase(
    val id: String = "",
    val eventoId: String = "",
    val simposioId: String? = null,
    val trabajoId: String? = null,
    val aulaId: String = "",
    val tipo: String = TipoActividad.OTRO.name,
    val titulo: String = "",
    val fecha: Timestamp? = null,
    val horaInicio: String = "",
    val horaFin: String = ""
) {
    companion object {
        const val MINUTOS_EXPOSICION = 20
        const val MINUTOS_PREGUNTAS = 10
        const val MINUTOS_PRESENTACION =
            MINUTOS_EXPOSICION + MINUTOS_PREGUNTAS
    }
}