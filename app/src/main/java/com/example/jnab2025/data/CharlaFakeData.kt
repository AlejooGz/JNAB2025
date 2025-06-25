package com.example.jnab2025.data

import com.example.jnab2025.models.Charla
import com.example.jnab2025.models.EstadoPropuesta

object CharlaFakeData {

    private val charlas = mutableListOf<Charla>(
        Charla(
            id = 0,
            titulo = "Test1",
            descripcion = "Test 1",
            nombreArchivo = "Archivo 1",
            fechaEnvio = "24/06/2024",
            estado = EstadoPropuesta.APROBADA,
            motivoRechazo = "",
            pagado = false,
            sala = "",
            horaInicio = "",
            horaFin = "",
            esFavorito = false,
            simposioId = 1,
            expositorId = 2,  // TODO: reemplazar con el expositor logueado
        ),
        Charla(
            id = 1,
            titulo = "Test2",
            descripcion = "Test 2",
            nombreArchivo = "Archivo 3",
            fechaEnvio = "24/06/2024",
            estado = EstadoPropuesta.APROBADA,
            motivoRechazo = "",
            pagado = true,
            sala = "",
            horaInicio = "",
            horaFin = "",
            esFavorito = false,
            simposioId = 2,
            expositorId = 3,  // TODO: reemplazar con el expositor logueado
        ),
        Charla(
            id = 2,
            titulo = "Test3",
            descripcion = "Test 3",
            nombreArchivo = "Archivo 3",
            fechaEnvio = "24/06/2024",
            estado = EstadoPropuesta.APROBADA,
            motivoRechazo = "",
            pagado = true,
            sala = "",
            horaInicio = "",
            horaFin = "",
            esFavorito = false,
            simposioId = 3,
            expositorId = 3,  // TODO: reemplazar con el expositor logueado
        ),
        Charla(
            id = 3,
            titulo = "Test4",
            descripcion = "Test 4",
            nombreArchivo = "Archivo 4",
            fechaEnvio = "24/06/2024",
            estado = EstadoPropuesta.APROBADA,
            motivoRechazo = "",
            pagado = true,
            sala = "",
            horaInicio = "",
            horaFin = "",
            esFavorito = false,
            simposioId = 4,
            expositorId = 2,  // TODO: reemplazar con el expositor logueado
        )
    )

    fun getCharlasDeEjemplo(): List<Charla> = charlas
}