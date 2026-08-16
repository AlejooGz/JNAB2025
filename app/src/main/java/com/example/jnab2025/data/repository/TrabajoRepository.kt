package com.example.jnab2025.data.repository

import com.example.jnab2025.data.local.dao.SimposioDao
import com.example.jnab2025.data.local.dao.TrabajoDao
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.SimposioConAula
import com.example.jnab2025.data.model.Trabajo
import com.example.jnab2025.data.model.TrabajoConEstado
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class TrabajoRepository(
    private val trabajos: TrabajoDao,
    private val simposios: SimposioDao
) {

    fun simposiosDisponibles(eventoId: Long): Flow<List<SimposioConAula>> =
        simposios.conAula(eventoId)

    /** Los trabajos del expositor con estado, programacion y si ya pago. */
    fun seguimiento(autorId: Long): Flow<List<TrabajoConEstado>> =
        trabajos.seguimiento(autorId, EstadoInscripcion.PAGADA)

    suspend fun porId(id: Long): Trabajo? = trabajos.porId(id)

    suspend fun actualizar(trabajo: Trabajo) = trabajos.actualizar(trabajo)

    suspend fun enviar(
        simposioId: Long,
        autorId: Long,
        titulo: String,
        resumen: String,
        archivoUri: String,
        nombreArchivo: String
    ): Long = trabajos.insertar(
        Trabajo(
            simposioId = simposioId,
            autorId = autorId,
            titulo = titulo,
            resumen = resumen,
            archivoUri = archivoUri,
            nombreArchivo = nombreArchivo,
            fechaEnvio = Instant.now(),
            estado = EstadoTrabajo.ENVIADO
        )
    )
}
