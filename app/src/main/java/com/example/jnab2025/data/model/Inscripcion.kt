package com.example.jnab2025.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * La inscripcion es de la persona al evento, no del trabajo. Un expositor con
 * dos trabajos paga una sola vez.
 */
@Entity(
    tableName = "inscripcion",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Evento::class,
            parentColumns = ["id"],
            childColumns = ["eventoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("eventoId"),
        Index(value = ["usuarioId", "eventoId"], unique = true)
    ]
)
data class Inscripcion(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuarioId: Long,
    val eventoId: Long,
    val tipo: TipoInscripcion,
    val estado: EstadoInscripcion = EstadoInscripcion.PENDIENTE_PAGO,
    val monto: Double,
    val fechaAlta: Instant
)

/** El comprobante que sube la persona y que despues verifica la organizacion. */
@Entity(
    tableName = "comprobante_pago",
    foreignKeys = [
        ForeignKey(
            entity = Inscripcion::class,
            parentColumns = ["id"],
            childColumns = ["inscripcionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["verificadoPorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["inscripcionId"], unique = true),
        Index("verificadoPorId")
    ]
)
data class ComprobantePago(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inscripcionId: Long,
    val archivoUri: String,
    val nombreArchivo: String,
    val fechaCarga: Instant,
    val estado: EstadoComprobante = EstadoComprobante.PENDIENTE,
    val verificadoPorId: Long? = null
)
