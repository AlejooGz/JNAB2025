package com.example.jnab2025.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.jnab2025.models.Simposio

@Dao
interface SimposioDao {
    @Query("SELECT * FROM simposios")
    fun obtenerTodos(): LiveData<List<Simposio>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(simposio: Simposio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(simposios: List<Simposio>)

    @Delete
    suspend fun eliminar(simposio: Simposio)

    @Query("DELETE FROM simposios")
    suspend fun eliminarTodos()
}