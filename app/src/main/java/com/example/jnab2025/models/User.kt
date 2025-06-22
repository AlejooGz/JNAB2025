package com.example.jnab2025.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val rol: String // Ej: "ASISTENTE", "EXPOSITOR", "ORGANIZADOR"
)
