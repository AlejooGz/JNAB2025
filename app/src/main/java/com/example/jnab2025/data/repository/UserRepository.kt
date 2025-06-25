package com.example.jnab2025.data.repository

import androidx.lifecycle.LiveData
import com.example.jnab2025.data.dao.UserDao
import com.example.jnab2025.models.Charla
import com.example.jnab2025.models.User

class UserRepository(private val userDao: UserDao) {
    suspend fun obtenerTodos(): List<User> = userDao.obtenerTodos()

    suspend fun insertar(user: User) = userDao.insertar(user)
    suspend fun insertarTodos(lista: List<User>) = userDao.insertarTodos(lista)
    suspend fun eliminar(user: User) = userDao.eliminar(user)
    suspend fun eliminarTodos() = userDao.eliminarTodos()
    suspend fun autenticar(username: String, password: String): User? =
        userDao.autenticar(username, password)
}
