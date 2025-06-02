package com.example.jnab2025.models
import com.google.android.gms.maps.model.LatLng
data class Lugar(
    val nombre: String,
    val ubicacion: LatLng,
    val categoria: Categoria
)

enum class Categoria {
    HOSPEDAJE, RESTAURANTE, AGENCIA
}