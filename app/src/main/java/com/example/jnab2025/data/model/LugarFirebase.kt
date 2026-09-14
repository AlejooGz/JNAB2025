package com.example.jnab2025.data.model

data class LugarFirebase(
    val id: String = "",
    val nombre: String = "",
    // en firestore se guarda como HOSPEDAJE,RESTAURANTE o AGENCIA
    val categoria: String = CategoriaLugar.HOSPEDAJE.name,
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val descuento: String = "",
    val direccion: String = "",
    val telefono: String = "",
    val email: String = "",
    val web: String = "",
    val activo: Boolean = true
) {
    fun categoriaEnum(): CategoriaLugar {
        return try {
            CategoriaLugar.valueOf(categoria)
        } catch (e: Exception) {
            CategoriaLugar.HOSPEDAJE
        }
    }
}