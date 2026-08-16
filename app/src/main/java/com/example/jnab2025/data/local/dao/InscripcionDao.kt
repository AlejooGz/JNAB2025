package com.example.jnab2025.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.jnab2025.data.model.ComprobantePago
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.Inscripcion
import com.example.jnab2025.data.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface InscripcionDao {

    @Insert
    suspend fun insertar(inscripcion: Inscripcion): Long

    @Update
    suspend fun actualizar(inscripcion: Inscripcion)

    @Query("SELECT * FROM inscripcion WHERE usuarioId = :usuarioId AND eventoId = :eventoId")
    suspend fun de(usuarioId: Long, eventoId: Long): Inscripcion?

    @Query("SELECT * FROM inscripcion WHERE usuarioId = :usuarioId AND eventoId = :eventoId")
    fun observar(usuarioId: Long, eventoId: Long): Flow<Inscripcion?>

    @Query("SELECT * FROM comprobante_pago WHERE inscripcionId = :inscripcionId")
    fun observarComprobante(inscripcionId: Long): Flow<ComprobantePago?>

    @Query("UPDATE inscripcion SET estado = :estado WHERE id = :inscripcionId")
    suspend fun cambiarEstado(inscripcionId: Long, estado: EstadoInscripcion)

    /** REPLACE porque hay un unico comprobante por inscripcion: si sube otro, pisa el anterior. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarComprobante(comprobante: ComprobantePago): Long

    @Query("SELECT * FROM comprobante_pago WHERE inscripcionId = :inscripcionId")
    suspend fun comprobanteDe(inscripcionId: Long): ComprobantePago?

    @Query(
        """
        UPDATE comprobante_pago SET estado = :estado, verificadoPorId = :organizadorId
        WHERE id = :comprobanteId
        """
    )
    suspend fun verificarComprobante(
        comprobanteId: Long,
        estado: EstadoComprobante,
        organizadorId: Long
    )

    /** Los inscriptos de verdad, para la pantalla del organizador. */
    @Query(
        """
        SELECT u.* FROM inscripcion i
        JOIN usuario u ON u.id = i.usuarioId
        WHERE i.eventoId = :eventoId
        ORDER BY u.apellido, u.nombre
        """
    )
    fun inscriptos(eventoId: Long): Flow<List<Usuario>>

    @Query("SELECT COUNT(*) FROM inscripcion WHERE eventoId = :eventoId AND estado = :estado")
    fun cantidadPorEstado(eventoId: Long, estado: EstadoInscripcion): Flow<Int>
}
