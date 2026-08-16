package com.example.jnab2025.data.repository

import com.example.jnab2025.data.local.JnabDatabase
import com.example.jnab2025.data.model.ComprobantePago
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.Evento
import com.example.jnab2025.data.model.Inscripcion
import com.example.jnab2025.data.model.TipoInscripcion
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class InscripcionRepository(db: JnabDatabase) {

    private val inscripciones = db.inscripcionDao()
    private val eventos = db.eventoDao()

    fun evento(eventoId: Long): Flow<Evento?> = eventos.porId(eventoId)

    fun inscripcion(usuarioId: Long, eventoId: Long): Flow<Inscripcion?> =
        inscripciones.observar(usuarioId, eventoId)

    fun comprobante(inscripcionId: Long): Flow<ComprobantePago?> =
        inscripciones.observarComprobante(inscripcionId)

    /** Los estudiantes pagan la mitad. */
    fun montoPara(tipo: TipoInscripcion, evento: Evento): Double =
        if (tipo == TipoInscripcion.ESTUDIANTE) evento.montoInscripcion / 2 else evento.montoInscripcion

    suspend fun inscribir(usuarioId: Long, evento: Evento, tipo: TipoInscripcion): Long =
        inscripciones.insertar(
            Inscripcion(
                usuarioId = usuarioId,
                eventoId = evento.id,
                tipo = tipo,
                monto = montoPara(tipo, evento),
                fechaAlta = Instant.now()
            )
        )

    /**
     * Guarda el comprobante como pendiente de verificacion. No marca la
     * inscripcion como pagada: eso lo decide la organizacion al revisarlo.
     */
    suspend fun cargarComprobante(inscripcionId: Long, archivoUri: String, nombreArchivo: String) =
        inscripciones.insertarComprobante(
            ComprobantePago(
                inscripcionId = inscripcionId,
                archivoUri = archivoUri,
                nombreArchivo = nombreArchivo,
                fechaCarga = Instant.now(),
                estado = EstadoComprobante.PENDIENTE
            )
        )
}
