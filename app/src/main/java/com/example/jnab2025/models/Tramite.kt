package com.example.jnab2025.models


data class Tramite(
    val id: Int,
    var tituloTrabajo: String,
    var estado: String = "Pendiente",
    var nombreArchivo: String,
    var resumen: String? = null,
    var pagado: Boolean = false,
    var fechaEnvio: String = "01/06/2025"
)

