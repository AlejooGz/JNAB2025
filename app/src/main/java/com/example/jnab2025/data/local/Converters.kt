package com.example.jnab2025.data.local

import androidx.room.TypeConverter
import com.example.jnab2025.data.model.CategoriaLugar
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.PublicoFaq
import com.example.jnab2025.data.model.Rol
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.data.model.TipoFecha
import com.example.jnab2025.data.model.TipoInscripcion
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Fechas y horas se guardan como texto ISO ("2025-05-15", "14:00") para que
 * ordenar y comparar en SQL funcione tal cual. Los instantes van como epoch millis.
 *
 * Requiere core library desugaring (ver build.gradle.kts) porque el minSdk es 24
 * y java.time entro recien en la API 26.
 */
class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromRol(value: Rol): String = value.name

    @TypeConverter
    fun toRol(value: String): Rol = Rol.valueOf(value)

    @TypeConverter
    fun fromEstadoTrabajo(value: EstadoTrabajo): String = value.name

    @TypeConverter
    fun toEstadoTrabajo(value: String): EstadoTrabajo = EstadoTrabajo.valueOf(value)

    @TypeConverter
    fun fromTipoActividad(value: TipoActividad): String = value.name

    @TypeConverter
    fun toTipoActividad(value: String): TipoActividad = TipoActividad.valueOf(value)

    @TypeConverter
    fun fromTipoInscripcion(value: TipoInscripcion): String = value.name

    @TypeConverter
    fun toTipoInscripcion(value: String): TipoInscripcion = TipoInscripcion.valueOf(value)

    @TypeConverter
    fun fromEstadoInscripcion(value: EstadoInscripcion): String = value.name

    @TypeConverter
    fun toEstadoInscripcion(value: String): EstadoInscripcion = EstadoInscripcion.valueOf(value)

    @TypeConverter
    fun fromEstadoComprobante(value: EstadoComprobante): String = value.name

    @TypeConverter
    fun toEstadoComprobante(value: String): EstadoComprobante = EstadoComprobante.valueOf(value)

    @TypeConverter
    fun fromTipoFecha(value: TipoFecha): String = value.name

    @TypeConverter
    fun toTipoFecha(value: String): TipoFecha = TipoFecha.valueOf(value)

    @TypeConverter
    fun fromCategoriaLugar(value: CategoriaLugar): String = value.name

    @TypeConverter
    fun toCategoriaLugar(value: String): CategoriaLugar = CategoriaLugar.valueOf(value)

    @TypeConverter
    fun fromPublicoFaq(value: PublicoFaq): String = value.name

    @TypeConverter
    fun toPublicoFaq(value: String): PublicoFaq = PublicoFaq.valueOf(value)
}
