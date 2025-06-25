package com.example.jnab2025.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agenda_charla")
data class AgendaCharlaEntity(
    @PrimaryKey val id: String, // ID de la charla (de Firebase o generada)
    val titulo: String,
    val descripcion: String,
    val fecha: String,       // ISO 8601 (por ejemplo: "2025-09-12")
    val hora: String,        // "14:00"
    val esExpositor: Boolean
)