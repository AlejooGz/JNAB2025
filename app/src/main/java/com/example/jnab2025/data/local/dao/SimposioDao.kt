package com.example.jnab2025.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jnab2025.data.model.Aula
import com.example.jnab2025.data.model.Simposio
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface SimposioDao {

    @Insert
    suspend fun insertar(simposio: Simposio): Long

    @Update
    suspend fun actualizar(simposio: Simposio)

    @Delete
    suspend fun eliminar(simposio: Simposio)

    @Query("SELECT * FROM simposio WHERE id = :id")
    suspend fun porId(id: Long): Simposio?

    @Query("SELECT * FROM simposio WHERE eventoId = :eventoId ORDER BY fechaInicio, titulo")
    fun delEvento(eventoId: Long): Flow<List<Simposio>>

    /** Los simposios de un organizador: lo que hoy no se puede filtrar. */
    @Query(
        """
        SELECT * FROM simposio
        WHERE eventoId = :eventoId AND organizadorId = :organizadorId
        ORDER BY fechaInicio, titulo
        """
    )
    fun deOrganizador(eventoId: Long, organizadorId: Long): Flow<List<Simposio>>

    @Query("SELECT * FROM aula WHERE id = (SELECT aulaId FROM simposio WHERE id = :simposioId)")
    suspend fun aulaDe(simposioId: Long): Aula?

    /**
     * Otros simposios que ya ocupan esa aula con fechas que se pisan.
     * Si devuelve algo, el aula no se puede asignar.
     */
    @Query(
        """
        SELECT * FROM simposio
        WHERE aulaId = :aulaId
          AND id <> :simposioId
          AND fechaInicio <= :fechaFin
          AND :fechaInicio <= fechaFin
        """
    )
    suspend fun conflictosDeAula(
        aulaId: Long,
        fechaInicio: LocalDate,
        fechaFin: LocalDate,
        simposioId: Long
    ): List<Simposio>
}
