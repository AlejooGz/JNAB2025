package com.example.jnab2025.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.Instant

/**
 * La agenda personal. Reemplaza al viejo campo esFavorito, que al vivir dentro
 * de la charla hacia que el favorito de una persona se le marcara a todas.
 */
@Entity(
    tableName = "agenda_usuario",
    primaryKeys = ["usuarioId", "charlaId"],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Charla::class,
            parentColumns = ["id"],
            childColumns = ["charlaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("charlaId")]
)
data class AgendaUsuario(
    val usuarioId: Long,
    val charlaId: Long,
    /** Minutos de anticipacion del recordatorio. */
    val recordatorioMinutos: Int = 15,
    val agregadoEn: Instant
)
