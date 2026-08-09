package com.example.jnab2025.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/** Los avisos del feed. La imagen es una URL, no un R.drawable. */
@Entity(
    tableName = "novedad",
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
data class Novedad(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventoId: Long,
    val titulo: String,
    val descripcion: String,
    val fechaPublicacion: Instant,
    val imagenUrl: String? = null
)

/** Hoteles, restaurantes y agencias del mapa de descuentos. */
@Entity(
    tableName = "lugar",
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
data class Lugar(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventoId: Long,
    val nombre: String,
    val categoria: CategoriaLugar,
    val latitud: Double,
    val longitud: Double,
    val descuento: String? = null
)

@Entity(
    tableName = "faq",
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
data class Faq(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventoId: Long,
    val publico: PublicoFaq,
    val pregunta: String,
    val respuesta: String,
    val orden: Int
)
