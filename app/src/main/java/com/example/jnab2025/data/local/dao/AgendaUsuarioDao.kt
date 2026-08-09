package com.example.jnab2025.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.jnab2025.data.model.AgendaUsuario
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaUsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregar(item: AgendaUsuario)

    @Query("DELETE FROM agenda_usuario WHERE usuarioId = :usuarioId AND charlaId = :charlaId")
    suspend fun quitar(usuarioId: Long, charlaId: Long)

    @Query(
        "SELECT EXISTS(SELECT 1 FROM agenda_usuario WHERE usuarioId = :usuarioId AND charlaId = :charlaId)"
    )
    suspend fun estaAgendada(usuarioId: Long, charlaId: Long): Boolean

    @Query("SELECT * FROM agenda_usuario WHERE usuarioId = :usuarioId")
    fun deUsuario(usuarioId: Long): Flow<List<AgendaUsuario>>

    @Query(
        """
        UPDATE agenda_usuario SET recordatorioMinutos = :minutos
        WHERE usuarioId = :usuarioId AND charlaId = :charlaId
        """
    )
    suspend fun cambiarRecordatorio(usuarioId: Long, charlaId: Long, minutos: Int)
}
