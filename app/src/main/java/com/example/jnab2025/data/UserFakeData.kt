package com.example.jnab2025.data

import com.example.jnab2025.models.Simposio
import com.example.jnab2025.models.User

object UserFakeData {

    private val users = listOf(
        User(id = 1, username = "admin", password = "1234", rol = "organizador"),
        User(id = 2, username = "usuario1", password = "pass1", rol = "expositor"),
        User(id = 3, username = "leo", password = "hola123", rol = "expositor"),
        User(id = 4, username = "usuario2", password = "pass2", rol = "asistente"),
        User(id = 5, username = "alejo", password = "hola123", rol = "asistente"),
        User(id = 6, username = "luciana", password = "hola123", rol = "asistente"),
        User(id = 7, username = "santiago", password = "hola123", rol = "asistente")
    )

    fun getUsersDeEjemplo(): List<User> = users

    fun login(username: String, password: String): User? {
        return users.find { it.username == username && it.password == password }
    }
}
