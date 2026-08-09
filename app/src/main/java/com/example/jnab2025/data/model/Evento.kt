package com.example.jnab2025.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/** Una edicion de las jornadas. Existe para que el modelo aguante la JNAB 2026. */
@Entity(tableName = "evento")
data class Evento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val fechaInicio: LocalDate,
    val fechaFin: LocalDate,
    val sede: String,
    val montoInscripcion: Double
)

/** Cierre de envio de resumenes, cierre de pago, etc. Base de los recordatorios. */
@Entity(
    tableName = "fecha_importante",
    foreignKeys = [
        ForeignKey(
            entity = Evento::class,
            parentColumns = ["id"],
            childColumns = ["eventoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("eventoId")]
)
data class FechaImportante(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventoId: Long,
    val titulo: String,
    val descripcion: String,
    val fechaLimite: Instant,
    val tipo: TipoFecha
)
