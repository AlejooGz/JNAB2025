package com.example.jnab2025.data

import com.example.jnab2025.models.User

object UserRepository {

    private val users = listOf(
        User(id = 0, username = "admin", password = "1234", rol = "organizador"),
        User(id = 0, username = "usuario1", password = "pass1", rol = "expositor"),
        User(id = 0, username = "usuario2", password = "pass2", rol = "asistente")
    )

    fun login(username: String, password: String): User? {
        return users.find { it.username == username && it.password == password }
    }
}
