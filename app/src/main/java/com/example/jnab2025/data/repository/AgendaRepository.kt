package com.example.jnab2025.data.repository

import androidx.lifecycle.LiveData
import com.example.jnab2025.data.dao.AgendaDao
import com.example.jnab2025.models.AgendaCharlaEntity

class AgendaRepository(private val dao: AgendaDao) {

    fun obtenerTodas(): LiveData<List<AgendaCharlaEntity>> = dao.obtenerTodas()

    fun obtenerPorFecha(fecha: String): LiveData<List<AgendaCharlaEntity>> =
        dao.obtenerPorFecha(fecha)

    suspend fun agregarCharla(charla: AgendaCharlaEntity) = dao.insertar(charla)

    suspend fun eliminarCharla(charla: AgendaCharlaEntity) = dao.eliminar(charla)

    suspend fun limpiarAgenda() = dao.eliminarTodo()
}