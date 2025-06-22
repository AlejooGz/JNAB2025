package com.example.jnab2025.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.jnab2025.models.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun obtenerTodos(): LiveData<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(user: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(users: List<User>)

    @Delete
    suspend fun eliminar(user: User)

    @Query("DELETE FROM users")
    suspend fun eliminarTodos()

    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun autenticar(username: String, password: String): User?
}
