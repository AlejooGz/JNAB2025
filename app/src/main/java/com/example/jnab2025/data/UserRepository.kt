package com.example.jnab2025.data

import com.example.jnab2025.models.User

object UserRepository {

    private val users = listOf(
        User("admin", "1234", "organizador"),
        User("usuario1", "pass1", "expositor"),
        User("usuario2", "pass2", "asistente")
    )

    fun login(username: String, password: String): User? {
        return users.find { it.username == username && it.password == password }
    }

}