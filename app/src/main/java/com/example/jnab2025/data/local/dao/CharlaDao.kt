package com.example.jnab2025.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jnab2025.data.model.Charla
import com.example.jnab2025.data.model.ItemAgenda
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime

@Dao
interface CharlaDao {

    @Insert
    suspend fun insertar(charla: Charla): Long

    @Update
    suspend fun actualizar(charla: Charla)

    @Delete
    suspend fun eliminar(charla: Charla)

    @Query("SELECT * FROM charla WHERE id = :id")
    suspend fun porId(id: Long): Charla?

    @Query("SELECT * FROM charla WHERE trabajoId = :trabajoId")
    suspend fun deTrabajo(trabajoId: Long): Charla?

    @Query("SELECT DISTINCT fecha FROM charla WHERE eventoId = :eventoId ORDER BY fecha")
    fun diasDelEvento(eventoId: Long): Flow<List<LocalDate>>

    /**
     * El cronograma de un dia, con el aula ya resuelta: la propia de la charla
     * si la tiene, si no la del simposio. Tambien dice si el usuario la tiene
     * agendada, asi la pantalla no necesita una segunda consulta.
     */
    @Query(
        """
        SELECT c.id        AS charlaId,
               c.titulo    AS titulo,
               c.tipo      AS tipo,
               c.fecha     AS fecha,
               c.horaInicio AS horaInicio,
               c.horaFin   AS horaFin,
               COALESCE(aulaCharla.nombre,   aulaSimposio.nombre)   AS aula,
               COALESCE(aulaCharla.edificio, aulaSimposio.edificio) AS edificio,
               COALESCE(aulaCharla.piso,     aulaSimposio.piso)     AS piso,
               s.titulo    AS simposio,
               u.nombre || ' ' || u.apellido AS expositor,
               ag.charlaId IS NOT NULL       AS enMiAgenda
        FROM charla c
        LEFT JOIN simposio s          ON s.id = c.simposioId
        LEFT JOIN aula aulaSimposio   ON aulaSimposio.id = s.aulaId
        LEFT JOIN aula aulaCharla     ON aulaCharla.id = c.aulaId
        LEFT JOIN trabajo t           ON t.id = c.trabajoId
        LEFT JOIN usuario u           ON u.id = t.autorId
        LEFT JOIN agenda_usuario ag   ON ag.charlaId = c.id AND ag.usuarioId = :usuarioId
        WHERE c.eventoId = :eventoId AND c.fecha = :fecha
        ORDER BY c.horaInicio, aula
        """
    )
    fun cronogramaDelDia(eventoId: Long, fecha: LocalDate, usuarioId: Long): Flow<List<ItemAgenda>>

    /** Lo mismo pero solo con lo que el usuario agendo: su agenda personal. */
    @Query(
        """
        SELECT c.id        AS charlaId,
               c.titulo    AS titulo,
               c.tipo      AS tipo,
               c.fecha     AS fecha,
               c.horaInicio AS horaInicio,
               c.horaFin   AS horaFin,
               COALESCE(aulaCharla.nombre,   aulaSimposio.nombre)   AS aula,
               COALESCE(aulaCharla.edificio, aulaSimposio.edificio) AS edificio,
               COALESCE(aulaCharla.piso,     aulaSimposio.piso)     AS piso,
               s.titulo    AS simposio,
               u.nombre || ' ' || u.apellido AS expositor,
               1           AS enMiAgenda
        FROM agenda_usuario ag
        JOIN charla c                 ON c.id = ag.charlaId
        LEFT JOIN simposio s          ON s.id = c.simposioId
        LEFT JOIN aula aulaSimposio   ON aulaSimposio.id = s.aulaId
        LEFT JOIN aula aulaCharla     ON aulaCharla.id = c.aulaId
        LEFT JOIN trabajo t           ON t.id = c.trabajoId
        LEFT JOIN usuario u           ON u.id = t.autorId
        WHERE ag.usuarioId = :usuarioId AND c.eventoId = :eventoId
        ORDER BY c.fecha, c.horaInicio
        """
    )
    fun miAgenda(eventoId: Long, usuarioId: Long): Flow<List<ItemAgenda>>

    /**
     * Charlas ya agendadas por el usuario que se pisan con el rango dado.
     * Sirve para avisarle antes de que agregue una charla que le solapa con otra.
     */
    @Query(
        """
        SELECT c.* FROM agenda_usuario ag
        JOIN charla c ON c.id = ag.charlaId
        WHERE ag.usuarioId = :usuarioId
          AND c.fecha = :fecha
          AND c.id <> :charlaId
          AND c.horaInicio < :horaFin
          AND :horaInicio < c.horaFin
        ORDER BY c.horaInicio
        """
    )
    suspend fun solapadasEnMiAgenda(
        usuarioId: Long,
        charlaId: Long,
        fecha: LocalDate,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): List<Charla>

    /**
     * Cualquier charla del mismo simposio que ocupe ese horario. Como el
     * simposio tiene un aula fija, esto es tambien el choque de aula.
     */
    @Query(
        """
        SELECT * FROM charla
        WHERE simposioId = :simposioId
          AND fecha = :fecha
          AND id <> :charlaId
          AND horaInicio < :horaFin
          AND :horaInicio < horaFin
        """
    )
    suspend fun solapadasEnSimposio(
        simposioId: Long,
        charlaId: Long,
        fecha: LocalDate,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ): List<Charla>
}
