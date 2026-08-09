package com.example.jnab2025.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

/**
 * Un bloque del cronograma: ocupa un dia y un rango horario.
 *
 * Es la unica tabla que arma la linea de tiempo del congreso, asi que cubre
 * tanto las presentaciones como el coffee break y la acreditacion. Por eso
 * [simposioId] y [trabajoId] son nullables:
 *
 *  - tipo PRESENTACION  -> simposioId y trabajoId con valor, aulaId en null
 *  - coffee break       -> simposioId y trabajoId en null, aulaId opcional
 *
 * Regla del aula: si [aulaId] es null, el aula sale del simposio. Se completa
 * solo para actividades que no pertenecen a ningun simposio, o cuando una
 * charla puntual se muda de sala.
 */
@Entity(
    tableName = "charla",
    foreignKeys = [
        ForeignKey(
            entity = Evento::class,
            parentColumns = ["id"],
            childColumns = ["eventoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Simposio::class,
            parentColumns = ["id"],
            childColumns = ["simposioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Trabajo::class,
            parentColumns = ["id"],
            childColumns = ["trabajoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Aula::class,
            parentColumns = ["id"],
            childColumns = ["aulaId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("eventoId"),
        Index("aulaId"),
        // Un trabajo no puede estar programado dos veces. SQLite considera
        // distintos entre si a los NULL, asi que las actividades sin trabajo
        // (coffee break) no chocan con este indice.
        Index(value = ["trabajoId"], unique = true),
        // Como el simposio tiene un aula fija, esto ya impide dos charlas
        // en la misma aula y horario.
        Index(value = ["simposioId", "fecha", "horaInicio"], unique = true)
    ]
)
data class Charla(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventoId: Long,
    val simposioId: Long? = null,
    val trabajoId: Long? = null,
    val aulaId: Long? = null,
    val tipo: TipoActividad,
    val titulo: String,
    val fecha: LocalDate,
    val horaInicio: LocalTime,
    val horaFin: LocalTime
) {
    companion object {
        /** 20 minutos de exposicion + 10 de preguntas, segun el enunciado. */
        const val MINUTOS_EXPOSICION = 20
        const val MINUTOS_PREGUNTAS = 10
        const val MINUTOS_PRESENTACION = MINUTOS_EXPOSICION + MINUTOS_PREGUNTAS

        /** Arma el bloque de 30 minutos a partir de la hora de inicio. */
        fun presentacion(
            eventoId: Long,
            simposioId: Long,
            trabajoId: Long,
            titulo: String,
            fecha: LocalDate,
            horaInicio: LocalTime
        ) = Charla(
            eventoId = eventoId,
            simposioId = simposioId,
            trabajoId = trabajoId,
            tipo = TipoActividad.PRESENTACION,
            titulo = titulo,
            fecha = fecha,
            horaInicio = horaInicio,
            horaFin = horaInicio.plusMinutes(MINUTOS_PRESENTACION.toLong())
        )
    }
}
