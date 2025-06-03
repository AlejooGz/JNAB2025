package com.example.jnab2025.models
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "simposios")
data class Simposio (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    var fechaInicio: String, // Formato "yyyy-MM-dd"
    var fechaFin: String, // Formato "yyyy-MM-dd"
)