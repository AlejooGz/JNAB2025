package com.example.jnab2025.data

import com.example.jnab2025.models.Charla

object CharlaRepository {
    private val charlas = mutableListOf(
        Charla(
            id = 1,
            titulo = "Conferencia Inaugural JNAB 2025",
            expositor = "Dr. Roberto García - Comité Organizador",
            fecha = "2025-05-15",
            horaInicio = "09:00",
            horaFin = "09:30",
            sala = "Auditorio Principal",
            esFavorito = false,
            esDestacado = true
        ),
        Charla(
            id = 2,
            titulo = "Innovaciones en IA para el Desarrollo de Software",
            expositor = "Dra. María Sánchez - Universidad Tecnológica",
            fecha = "2025-05-15",
            horaInicio = "14:00",
            horaFin = "14:30",
            sala = "Sala 3",
            esFavorito = true,
            esDestacado = true
        ),
        Charla(
            id = 3,
            titulo = "Frameworks para Desarrollo Móvil",
            expositor = "Ing. Carlos López - Tech Mobile Inc.",
            fecha = "2025-05-16",
            horaInicio = "10:30",
            horaFin = "11:00",
            sala = "Sala 2",
            esFavorito = false,
            esDestacado = false
        ),
        Charla(
            id = 4,
            titulo = "Networking: Conectando Profesionales",
            expositor = "Lic. Ana Martínez - Consultora de RRHH",
            fecha = "2025-05-16",
            horaInicio = "16:00",
            horaFin = "16:30",
            sala = "Hall Central",
            esFavorito = true,
            esDestacado = true
        ),
        Charla(
            id = 5,
            titulo = "Nuevas Tecnologías en la Nube",
            expositor = "Ing. Pedro Ramírez - CloudTech (Sponsor Principal)",
            fecha = "2025-05-17",
            horaInicio = "11:00",
            horaFin = "11:30",
            sala = "Auditorio Principal",
            esFavorito = false,
            esDestacado = true
        ),
        Charla(
            id = 6,
            titulo = "Panel: Tendencias en Ciberseguridad",
            expositor = "Panel de Expertos - Moderador: Dr. Javier Díaz",
            fecha = "2025-05-17",
            horaInicio = "14:30",
            horaFin = "15:00",
            sala = "Sala 4",
            esFavorito = false,
            esDestacado = false
        ),
        Charla(
            id = 7,
            titulo = "Ceremonia de Clausura JNAB 2025",
            expositor = "Comité Organizador",
            fecha = "2025-05-17",
            horaInicio = "18:00",
            horaFin = "18:30",
            sala = "Auditorio Principal",
            esFavorito = true,
            esDestacado = true
        )
    )

    fun obtenerTodasLasCharlas(): List<Charla> {
        return charlas.toList()
    }

    fun obtenerCharlasPorFecha(fecha: String): List<Charla> {
        return charlas.filter { it.fecha == fecha }
    }

    fun obtenerCharlasFavoritas(): List<Charla> {
        return charlas.filter { it.esFavorito }
    }

    fun obtenerCharlasDestacadas(): List<Charla> {
        return charlas.filter { it.esDestacado }
    }

    fun toggleFavorito(charlaId: Int): Boolean {
        val charla = charlas.find { it.id == charlaId }
        charla?.let {
            it.esFavorito = !it.esFavorito
            return it.esFavorito
        }
        return false
    }

    fun buscarCharlasPorTitulo(query: String): List<Charla> {
        return charlas.filter {
            it.titulo.contains(query, ignoreCase = true)
        }
    }

    fun buscarCharlasPorExpositor(query: String): List<Charla> {
        return charlas.filter {
            it.expositor.contains(query, ignoreCase = true)
        }
    }

    fun buscarCharlasPorSala(sala: String): List<Charla> {
        return charlas.filter { it.sala == sala }
    }

    // Método para agregar charlas desde propuestas aceptadas
    fun agregarCharlaDesdeProuesta(
        titulo: String,
        expositor: String,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        sala: String,
        esDestacado: Boolean = false
    ): Charla {
        val nuevaCharla = Charla(
            id = charlas.size + 1,
            titulo = titulo,
            expositor = expositor,
            fecha = fecha,
            horaInicio = horaInicio,
            horaFin = horaFin,
            sala = sala,
            esFavorito = false,
            esDestacado = esDestacado
        )
        charlas.add(nuevaCharla)
        return nuevaCharla
    }

    fun obtenerCharlaPorId(id: Int): Charla? {
        return charlas.find { it.id == id }
    }

    fun actualizarCharla(charlaActualizada: Charla) {
        val index = charlas.indexOfFirst { it.id == charlaActualizada.id }
        if (index != -1) {
            charlas[index] = charlaActualizada
        }
    }

    fun eliminarCharla(id: Int): Boolean {
        return charlas.removeIf { it.id == id }
    }
}