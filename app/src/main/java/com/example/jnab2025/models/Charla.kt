package com.example.jnab2025.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "charlas",
    foreignKeys = [
        ForeignKey(entity = Simposio::class, parentColumns = ["id"], childColumns = ["simposioId"]),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["expositorId"])
    ],
    indices = [Index("simposioId"), Index("expositorId")]
)
data class Charla(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val simposioId: Int,
    val titulo: String,
    val expositorId: Int,
    val descripcion: String,
    val nombreArchivo: String,
    val fechaEnvio: String,
    val estado: EstadoPropuesta,
    val pagado: Boolean = false,
    val fechaExposicion: String? = null,
    val horaInicio: String? = null,
    val horaFin: String? = null,
    val sala: String? = null,
    val esFavorito: Boolean = false
)

enum class EstadoPropuesta {
    PENDIENTE,
    APROBADA,
    RECHAZADA
}
