package com.example.jnab2025.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Eje tematico del congreso. Tiene un aula fija: todas las charlas del simposio
 * se dictan en la misma sala, tal como plantea el enunciado ("varios simposios
 * simultaneos en distintas aulas del edificio").
 *
 * Que el aula viva aca y no en cada charla tiene una ventaja concreta: el choque
 * de aulas se valida una sola vez, al crear o editar el simposio, en vez de en
 * cada charla que se programa.
 */
@Entity(
    tableName = "simposio",
    foreignKeys = [
        ForeignKey(
            entity = Evento::class,
            parentColumns = ["id"],
            childColumns = ["eventoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["organizadorId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = Aula::class,
            parentColumns = ["id"],
            childColumns = ["aulaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("eventoId"), Index("organizadorId"), Index("aulaId")]
)
data class Simposio(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventoId: Long,
    val organizadorId: Long,
    val aulaId: Long,
    val titulo: String,
    val descripcion: String,
    val temaCentral: String,
    val fechaInicio: LocalDate,
    val fechaFin: LocalDate
)
