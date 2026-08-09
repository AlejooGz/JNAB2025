package com.example.jnab2025.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.jnab2025.data.model.Rol
import com.example.jnab2025.data.model.Usuario
import com.example.jnab2025.data.model.UsuarioRol
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun asignarRol(usuarioRol: UsuarioRol)

    @Update
    suspend fun actualizar(usuario: Usuario)

    @Query("SELECT * FROM usuario WHERE id = :id")
    suspend fun porId(id: Long): Usuario?

    @Query("SELECT * FROM usuario WHERE email = :email LIMIT 1")
    suspend fun porEmail(email: String): Usuario?

    /** El login compara el hash, nunca la contrasena en texto plano. */
    @Query("SELECT * FROM usuario WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun autenticar(email: String, passwordHash: String): Usuario?

    @Query("SELECT rol FROM usuario_rol WHERE usuarioId = :usuarioId")
    suspend fun rolesDe(usuarioId: Long): List<Rol>

    @Query("SELECT EXISTS(SELECT 1 FROM usuario_rol WHERE usuarioId = :usuarioId AND rol = :rol)")
    suspend fun tieneRol(usuarioId: Long, rol: Rol): Boolean

    @Query("SELECT * FROM usuario ORDER BY apellido, nombre")
    fun todos(): Flow<List<Usuario>>
}
