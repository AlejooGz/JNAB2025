package com.example.jnab2025.data.repository

import androidx.room.withTransaction
import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.Aula
import com.example.jnab2025.data.model.Charla
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.PropuestaPendiente
import com.example.jnab2025.data.model.Simposio
import com.example.jnab2025.data.model.SimposioConAula
import com.example.jnab2025.data.model.Usuario
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/** Resultado de intentar programar una propuesta aprobada. */
sealed interface Aprobacion {
    data class Ok(val fecha: LocalDate, val desde: LocalTime, val hasta: LocalTime) : Aprobacion
    data class FueraDelSimposio(val desde: LocalDate, val hasta: LocalDate) : Aprobacion
    data class HorarioOcupado(val titulo: String, val desde: LocalTime, val hasta: LocalTime) : Aprobacion
    data object NoExiste : Aprobacion
}

class OrganizadorRepository(private val db: JnabDatabase) {

    private val simposios = db.simposioDao()
    private val trabajos = db.trabajoDao()
    private val charlas = db.charlaDao()
    private val inscripciones = db.inscripcionDao()
    private val eventos = db.eventoDao()

    fun misSimposios(eventoId: Long, organizadorId: Long): Flow<List<SimposioConAula>> =
        simposios.conAulaDeOrganizador(eventoId, organizadorId)

    fun propuestasPendientes(simposioId: Long): Flow<List<PropuestaPendiente>> =
        trabajos.propuestas(simposioId, EstadoTrabajo.ENVIADO)

    fun inscriptos(eventoId: Long): Flow<List<Usuario>> = inscripciones.inscriptos(eventoId)

    fun aulas(): Flow<List<Aula>> = eventos.aulas()

    suspend fun simposio(id: Long): Simposio? = simposios.porId(id)

    suspend fun crearSimposio(simposio: Simposio): Long = simposios.insertar(simposio)

    suspend fun actualizarSimposio(simposio: Simposio) = simposios.actualizar(simposio)

    /**
     * Otros simposios del mismo organizador o de otro que ya usan esa aula en
     * fechas que se pisan. Como cada simposio tiene un aula fija, este chequeo
     * reemplaza al que antes habria que hacer charla por charla.
     */
    suspend fun conflictosDeAula(
        aulaId: Long,
        desde: LocalDate,
        hasta: LocalDate,
        simposioId: Long
    ): List<Simposio> = simposios.conflictosDeAula(aulaId, desde, hasta, simposioId)

    /**
     * Aprueba el trabajo y le crea la charla en el cronograma. Las dos cosas van
     * juntas: un trabajo aprobado sin horario no le sirve a nadie.
     */
    suspend fun aprobar(
        trabajoId: Long,
        organizadorId: Long,
        fecha: LocalDate,
        horaInicio: LocalTime
    ): Aprobacion {
        val trabajo = trabajos.porId(trabajoId) ?: return Aprobacion.NoExiste
        val simposio = simposios.porId(trabajo.simposioId) ?: return Aprobacion.NoExiste

        if (fecha < simposio.fechaInicio || fecha > simposio.fechaFin) {
            return Aprobacion.FueraDelSimposio(simposio.fechaInicio, simposio.fechaFin)
        }

        val horaFin = horaInicio.plusMinutes(Charla.MINUTOS_PRESENTACION.toLong())
        val ocupado = charlas.solapadasEnSimposio(
            simposioId = simposio.id,
            charlaId = 0L,
            fecha = fecha,
            horaInicio = horaInicio,
            horaFin = horaFin
        ).firstOrNull()

        if (ocupado != null) {
            return Aprobacion.HorarioOcupado(ocupado.titulo, ocupado.horaInicio, ocupado.horaFin)
        }

        db.withTransaction {
            charlas.insertar(
                Charla.presentacion(
                    eventoId = simposio.eventoId,
                    simposioId = simposio.id,
                    trabajoId = trabajo.id,
                    titulo = trabajo.titulo,
                    fecha = fecha,
                    horaInicio = horaInicio
                )
            )
            trabajos.resolver(
                trabajoId = trabajo.id,
                estado = EstadoTrabajo.APROBADO,
                fecha = Instant.now(),
                organizadorId = organizadorId,
                motivo = null
            )
        }

        return Aprobacion.Ok(fecha, horaInicio, horaFin)
    }

    suspend fun rechazar(trabajoId: Long, organizadorId: Long, motivo: String) {
        trabajos.resolver(
            trabajoId = trabajoId,
            estado = EstadoTrabajo.RECHAZADO,
            fecha = Instant.now(),
            organizadorId = organizadorId,
            motivo = motivo
        )
    }

    suspend fun trabajo(id: Long) = trabajos.porId(id)
}
