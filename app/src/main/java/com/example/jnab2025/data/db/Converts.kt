package com.example.jnab2025.data.db

import androidx.room.TypeConverter
import com.example.jnab2025.models.EstadoPropuesta

class Converters {

    @TypeConverter
    fun fromEstadoPropuesta(value: EstadoPropuesta): String = value.name

    @TypeConverter
    fun toEstadoPropuesta(value: String): EstadoPropuesta = EstadoPropuesta.valueOf(value)
}
