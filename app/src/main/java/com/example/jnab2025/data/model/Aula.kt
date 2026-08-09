package com.example.jnab2025.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Aula del edificio donde se dicta un simposio.
 *
 * No tiene latitud ni longitud a proposito: todas las aulas estan en el mismo
 * edificio, asi que para armar la ruta entre dos charlas alcanza con edificio,
 * piso y una referencia en texto. Las coordenadas viven en [Lugar], que es el
 * mapa de descuentos de la ciudad.
 */
@Entity(tableName = "aula")
data class Aula(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val edificio: String,
    val piso: Int,
    val capacidad: Int,
    val referencia: String? = null
)
