package com.example.jnab2025.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.jnab2025.models.AgendaCharlaEntity

@Dao
interface AgendaDao {

    @Query("SELECT * FROM agenda_charla ORDER BY fecha, hora")
    fun obtenerTodas(): LiveData<List<AgendaCharlaEntity>>

    @Query("SELECT * FROM agenda_charla WHERE fecha = :fecha ORDER BY hora")
    fun obtenerPorFecha(fecha: String): LiveData<List<AgendaCharlaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(charla: AgendaCharlaEntity)

    @Delete
    suspend fun eliminar(charla: AgendaCharlaEntity)

    @Query("DELETE FROM agenda_charla")
    suspend fun eliminarTodo()
}