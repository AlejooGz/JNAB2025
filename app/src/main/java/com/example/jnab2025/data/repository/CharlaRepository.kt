package com.example.jnab2025.data.repository

import com.example.jnab2025.data.dao.CharlaDao
import com.example.jnab2025.models.Charla
import com.example.jnab2025.models.EstadoPropuesta

class CharlaRepository(private val charlaDao: CharlaDao) {

    suspend fun obtenerTodas(): List<Charla> = charlaDao.obtenerTodas()

    suspend fun crearCharla(charla: Charla) = charlaDao.insertarCharla(charla)

    suspend fun actualizarCharla(charla: Charla) = charlaDao.actualizarCharla(charla)

    suspend fun obtenerPorSimposio(id: Int) = charlaDao.obtenerCharlasPorSimposio(id)

    suspend fun obtenerPorExpositor(id: Int) = charlaDao.obtenerCharlasPorExpositor(id)

    suspend fun obtenerPorEstado(estado: EstadoPropuesta) = charlaDao.obtenerCharlasPorEstado(estado)

    suspend fun obtenerPorId(id: Int) = charlaDao.obtenerCharlaPorId(id)
}
