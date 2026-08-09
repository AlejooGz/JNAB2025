package com.example.jnab2025.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuario",
    indices = [Index(value = ["email"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val apellido: String,
    val email: String,
    val passwordHash: String,
    val institucion: String? = null
) {
    val nombreCompleto: String get() = "$nombre $apellido"
}

/**
 * Tabla puente entre usuario y sus roles. Va aparte y no como columna de [Usuario]
 * porque una misma persona expone y asiste al mismo tiempo.
 */
@Entity(
    tableName = "usuario_rol",
    primaryKeys = ["usuarioId", "rol"],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UsuarioRol(
    val usuarioId: Long,
    val rol: Rol
)
