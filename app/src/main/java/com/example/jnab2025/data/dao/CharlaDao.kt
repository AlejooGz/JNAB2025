package com.example.jnab2025.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.jnab2025.models.Charla
import com.example.jnab2025.models.EstadoPropuesta

@Dao
interface CharlaDao {
    @Query("SELECT * FROM charlas")
    suspend fun obtenerTodas(): List<Charla>

    @Insert
    suspend fun insertarCharla(charla: Charla)

    @Update
    suspend fun actualizarCharla(charla: Charla)

    @Query("SELECT * FROM charlas WHERE simposioId = :simposioId")
    suspend fun obtenerCharlasPorSimposio(simposioId: Int): List<Charla>

    @Query("SELECT * FROM charlas WHERE expositorId = :expositorId")
    suspend fun obtenerCharlasPorExpositor(expositorId: Int): List<Charla>

    @Query("SELECT * FROM charlas WHERE estado = :estado")
    suspend fun obtenerCharlasPorEstado(estado: EstadoPropuesta): List<Charla>

    @Query("SELECT * FROM charlas WHERE id = :id")
    suspend fun obtenerCharlaPorId(id: Int): Charla?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(charlas: List<Charla>)

    @Delete
    suspend fun eliminar(charla: Charla)

    @Query("DELETE FROM charlas")
    suspend fun eliminarTodos()
}
