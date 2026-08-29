package com.example.jnab2025.data.repository

import com.example.jnab2025.data.local.dao.SimposioDao
import com.example.jnab2025.data.model.Simposio
import kotlinx.coroutines.flow.Flow

class SimposioRepository(
    private val simposioDao: SimposioDao
) {

    fun simposiosDelEvento(eventoId: Long): Flow<List<Simposio>> =
        simposioDao.delEvento(eventoId)

    suspend fun insertar(simposio: Simposio): Long =
        simposioDao.insertar(simposio)

    suspend fun actualizar(simposio: Simposio) {
        simposioDao.actualizar(simposio)
    }

    suspend fun eliminar(simposio: Simposio) {
        simposioDao.eliminar(simposio)
    }

    suspend fun porId(id: Long): Simposio? =
        simposioDao.porId(id)

    fun simposiosDeOrganizador(
        eventoId: Long,
        organizadorId: Long
    ): Flow<List<Simposio>> =
        simposioDao.deOrganizador(eventoId, organizadorId)

    suspend fun aulaDe(simposioId: Long) =
        simposioDao.aulaDe(simposioId)

    fun simposiosConAula(
        eventoId: Long
    ) = simposioDao.conAula(eventoId)

    fun simposiosConAulaDeOrganizador(
        eventoId: Long,
        organizadorId: Long
    ) = simposioDao.conAulaDeOrganizador(eventoId, organizadorId)

    suspend fun conflictosDeAula(
        aulaId: Long,
        fechaInicio: java.time.LocalDate,
        fechaFin: java.time.LocalDate,
        simposioId: Long
    ) = simposioDao.conflictosDeAula(
        aulaId,
        fechaInicio,
        fechaFin,
        simposioId
    )
}