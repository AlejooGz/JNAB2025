package com.example.jnab2025.data.model

import java.time.LocalDate
import java.time.LocalTime

/**
 * Una fila del cronograma lista para mostrar: ya trae resuelto el aula
 * (la propia si la tiene, si no la del simposio) y el nombre del expositor.
 */
data class ItemAgenda(
    val charlaId: Long,
    val titulo: String,
    val tipo: TipoActividad,
    val fecha: LocalDate,
    val horaInicio: LocalTime,
    val horaFin: LocalTime,
    val aula: String?,
    val edificio: String?,
    val piso: Int?,
    val simposio: String?,
    val expositor: String?,
    val enMiAgenda: Boolean
)

/** El estado de un trabajo tal como lo ve el expositor en "Mis trabajos". */
data class TrabajoConEstado(
    val trabajoId: Long,
    val titulo: String,
    val nombreArchivo: String,
    val estado: EstadoTrabajo,
    val motivoRechazo: String?,
    val simposio: String,
    val fecha: LocalDate?,
    val horaInicio: LocalTime?,
    val aula: String?,
    val inscripcionPagada: Boolean
)

/** Una propuesta pendiente tal como la ve el organizador. */
data class PropuestaPendiente(
    val trabajoId: Long,
    val titulo: String,
    val resumen: String,
    val nombreArchivo: String,
    val autor: String,
    val autorEmail: String,
    val institucion: String?
)
