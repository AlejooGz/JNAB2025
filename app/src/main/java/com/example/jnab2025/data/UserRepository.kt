package com.example.jnab2025.data

import com.example.jnab2025.models.User

object UserRepository {

    private val users = listOf(
        User("admin", "1234", "Organizador"),
        User("usuario1", "pass1", "Expositor"),
        User("usuario2", "pass2", "Asistente")
    )

    fun login(username: String, password: String): Boolean {
        return users.any { it.username == username && it.password == password }
    }
}