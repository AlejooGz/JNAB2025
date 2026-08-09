package com.example.jnab2025.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.jnab2025.data.model.Aula
import com.example.jnab2025.data.model.Evento
import com.example.jnab2025.data.model.FechaImportante
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface EventoDao {

    @Insert
    suspend fun insertar(evento: Evento): Long

    @Query("SELECT * FROM evento ORDER BY fechaInicio DESC LIMIT 1")
    suspend fun actual(): Evento?

    @Query("SELECT * FROM evento WHERE id = :id")
    fun porId(id: Long): Flow<Evento?>

    @Insert
    suspend fun insertarAula(aula: Aula): Long

    @Query("SELECT * FROM aula ORDER BY edificio, piso, nombre")
    fun aulas(): Flow<List<Aula>>

    @Query("SELECT * FROM aula WHERE id = :id")
    suspend fun aulaPorId(id: Long): Aula?

    @Insert
    suspend fun insertarFecha(fecha: FechaImportante): Long

    @Query("SELECT * FROM fecha_importante WHERE eventoId = :eventoId ORDER BY fechaLimite")
    fun fechasImportantes(eventoId: Long): Flow<List<FechaImportante>>

    /** Las que todavia no vencieron: la base de los recordatorios. */
    @Query(
        """
        SELECT * FROM fecha_importante
        WHERE eventoId = :eventoId AND fechaLimite >= :desde
        ORDER BY fechaLimite
        """
    )
    suspend fun fechasPendientes(eventoId: Long, desde: Instant): List<FechaImportante>
}
