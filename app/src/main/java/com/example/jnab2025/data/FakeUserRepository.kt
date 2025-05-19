package com.example.jnab2025.data

import com.example.jnab2025.models.User

object FakeUserRepository {

    private val users = listOf(
        User("admin", "1234"),
        User("usuario1", "pass1"),
        User("usuario2", "pass2")
    )

    fun login(username: String, password: String): Boolean {
        return users.any { it.username == username && it.password == password }
    }
}