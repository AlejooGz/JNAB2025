package com.example.jnab2025.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Lo que envia el expositor y evalua el organizador. Todavia no tiene fecha,
 * hora ni aula: eso aparece recien cuando se aprueba y se le crea una [Charla].
 */
@Entity(
    tableName = "trabajo",
    foreignKeys = [
        ForeignKey(
            entity = Simposio::class,
            parentColumns = ["id"],
            childColumns = ["simposioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["autorId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["resueltoPorId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("simposioId"), Index("autorId"), Index("resueltoPorId")]
)
data class Trabajo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val simposioId: Long,
    val autorId: Long,
    val titulo: String,
    val resumen: String,
    /** URI persistible del PDF, no solo el nombre: asi el archivo sobrevive al cierre de la app. */
    val archivoUri: String,
    val nombreArchivo: String,
    val fechaEnvio: Instant,
    val estado: EstadoTrabajo = EstadoTrabajo.ENVIADO,
    val motivoRechazo: String? = null,
    val fechaResolucion: Instant? = null,
    val resueltoPorId: Long? = null
)
