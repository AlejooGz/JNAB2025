package com.example.jnab2025.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.PropuestaPendiente
import com.example.jnab2025.data.model.Trabajo
import com.example.jnab2025.data.model.TrabajoConEstado
import kotlinx.coroutines.flow.Flow
import java.time.Instant

@Dao
interface TrabajoDao {

    @Insert
    suspend fun insertar(trabajo: Trabajo): Long

    @Update
    suspend fun actualizar(trabajo: Trabajo)

    @Query("SELECT * FROM trabajo WHERE id = :id")
    suspend fun porId(id: Long): Trabajo?

    @Query("SELECT * FROM trabajo WHERE autorId = :autorId ORDER BY fechaEnvio DESC")
    fun deAutor(autorId: Long): Flow<List<Trabajo>>

    /** Marca el trabajo como aprobado. La charla se crea aparte, en la misma transaccion. */
    @Query(
        """
        UPDATE trabajo
        SET estado = :estado, fechaResolucion = :fecha, resueltoPorId = :organizadorId,
            motivoRechazo = :motivo
        WHERE id = :trabajoId
        """
    )
    suspend fun resolver(
        trabajoId: Long,
        estado: EstadoTrabajo,
        fecha: Instant,
        organizadorId: Long,
        motivo: String?
    )

    /** Lo que ve el organizador en "Ver propuestas" de un simposio. */
    @Query(
        """
        SELECT t.id                        AS trabajoId,
               t.titulo                    AS titulo,
               t.resumen                   AS resumen,
               t.nombreArchivo             AS nombreArchivo,
               t.archivoUri                AS archivoUri,
               u.nombre || ' ' || u.apellido AS autor,
               u.email                     AS autorEmail,
               u.institucion               AS institucion
        FROM trabajo t
        JOIN usuario u ON u.id = t.autorId
        WHERE t.simposioId = :simposioId AND t.estado = :estado
        ORDER BY t.fechaEnvio
        """
    )
    fun propuestas(simposioId: Long, estado: EstadoTrabajo): Flow<List<PropuestaPendiente>>

    /**
     * Lo que ve el expositor en "Mis trabajos": estado, donde y cuando expone,
     * y si su inscripcion al evento ya esta paga.
     */
    @Query(
        """
        SELECT t.id             AS trabajoId,
               t.titulo         AS titulo,
               t.nombreArchivo  AS nombreArchivo,
               t.estado         AS estado,
               t.motivoRechazo  AS motivoRechazo,
               s.titulo         AS simposio,
               c.fecha          AS fecha,
               c.horaInicio     AS horaInicio,
               au.nombre        AS aula,
               EXISTS(
                   SELECT 1 FROM inscripcion i
                   WHERE i.usuarioId = t.autorId
                     AND i.eventoId = s.eventoId
                     AND i.estado = :estadoPagada
               )                AS inscripcionPagada,
               EXISTS(
                   SELECT 1 FROM inscripcion i
                   JOIN comprobante_pago cp ON cp.inscripcionId = i.id
                   WHERE i.usuarioId = t.autorId
                     AND i.eventoId = s.eventoId
               )                AS comprobanteEnviado
        FROM trabajo t
        JOIN simposio s ON s.id = t.simposioId
        LEFT JOIN charla c ON c.trabajoId = t.id
        LEFT JOIN aula au ON au.id = COALESCE(c.aulaId, s.aulaId)
        WHERE t.autorId = :autorId
        ORDER BY t.fechaEnvio DESC
        """
    )
    fun seguimiento(
        autorId: Long,
        estadoPagada: EstadoInscripcion
    ): Flow<List<TrabajoConEstado>>
}
