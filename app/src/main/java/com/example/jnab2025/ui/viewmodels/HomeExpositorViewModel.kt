package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.data.model.TrabajoFirebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

/**
 * Datos propios del home del expositor: sus trabajos y sus presentaciones.
 * Lo que comparte con el asistente (inscripcion, agenda, cuenta regresiva)
 * lo sigue resolviendo [HomeAsistenteViewModel].
 */
class HomeExpositorViewModel(
    application: Application
) : AndroidViewModel(application) {

    /** Una charla generada a partir de un trabajo aprobado del expositor. */
    data class Presentacion(
        val charlaId: String,
        val titulo: String,
        val fecha: LocalDate,
        val horaInicio: LocalTime,
        val horaFin: LocalTime,
        val aula: String?,
        val simposio: String?
    )

    data class TrabajoResuelto(
        val id: String,
        val titulo: String,
        val estado: EstadoTrabajo,
        val motivoRechazo: String?,
        val fecha: LocalDate?
    )

    data class Estado(
        val cargando: Boolean = true,
        val enRevision: Int = 0,
        /** Aprobados + aceptados que esperan el pago. */
        val aceptados: Int = 0,
        val rechazados: Int = 0,
        /** Aceptados academicamente que no se programan hasta que pague. */
        val pendientesDePago: Int = 0,
        /** Aprobados que la organizacion todavia no ubico en el cronograma. */
        val sinProgramar: Int = 0,
        val presentaciones: List<Presentacion> = emptyList(),
        val resueltos: List<TrabajoResuelto> = emptyList()
    )

    companion object {
        const val MAX_RESUELTOS = 3
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()

    private val listeners = mutableListOf<ListenerRegistration>()

    private var trabajos: List<TrabajoFirebase> = emptyList()
    private var charlas: List<CharlaFirebase> = emptyList()
    private var simposios: List<SimposioFirebase> = emptyList()
    private val pendientes = mutableSetOf("trabajos", "charlas")

    init {
        if (uid == null) {
            _estado.value = Estado(cargando = false)
        } else {
            escucharTrabajos(uid)
            escucharCharlas()
            escucharSimposios()
        }
    }

    private fun escucharTrabajos(uid: String) {
        listeners += firestore
            .collection("trabajos")
            .whereEqualTo("autorUid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _avisos.trySend("No se pudieron cargar tus trabajos: ${error.message}")
                } else {
                    trabajos = snapshot
                        ?.documents
                        ?.mapNotNull { documento ->
                            documento
                                .toObject(TrabajoFirebase::class.java)
                                ?.copy(id = documento.id)
                        }
                        .orEmpty()
                }
                listo("trabajos")
            }
    }

    /* Se escuchan todas las charlas y se filtran aca por trabajoId: una
     * consulta whereIn por los ids de los trabajos habria que rehacerla cada
     * vez que cambian los trabajos, y tiene un tope de 30 valores. */
    private fun escucharCharlas() {
        listeners += firestore
            .collection("charlas")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _avisos.trySend("No se pudo cargar el cronograma: ${error.message}")
                } else {
                    charlas = snapshot
                        ?.documents
                        ?.mapNotNull { it.toObject(CharlaFirebase::class.java) }
                        .orEmpty()
                }
                listo("charlas")
            }
    }

    private fun escucharSimposios() {
        listeners += firestore
            .collection("simposios")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                simposios = snapshot
                    ?.documents
                    ?.mapNotNull { it.toObject(SimposioFirebase::class.java) }
                    .orEmpty()
                reconstruir()
            }
    }

    private fun listo(listener: String) {
        pendientes.remove(listener)
        reconstruir()
    }

    private fun reconstruir() {
        val estados = trabajos.map { it.estadoEnum() }
        val idsTrabajos = trabajos.map { it.id }.toSet()

        val misCharlas = charlas.filter { it.trabajoId in idsTrabajos }
        val programados = misCharlas.mapNotNull { it.trabajoId }.toSet()

        val presentaciones = misCharlas
            .mapNotNull { it.aPresentacion() }
            .sortedWith(compareBy({ it.fecha }, { it.horaInicio }))

        val resueltos = trabajos
            .filter { it.fechaResolucion != null }
            .filter { it.estadoEnum() in RESUELTOS }
            .sortedByDescending { it.fechaResolucion }
            .take(MAX_RESUELTOS)
            .map {
                TrabajoResuelto(
                    id = it.id,
                    titulo = it.titulo,
                    estado = it.estadoEnum(),
                    motivoRechazo = it.motivoRechazo,
                    fecha = it.fechaResolucion?.aLocalDate()
                )
            }

        _estado.value = Estado(
            cargando = pendientes.isNotEmpty(),
            enRevision = estados.count {
                it == EstadoTrabajo.ENVIADO || it == EstadoTrabajo.EN_EVALUACION
            },
            aceptados = estados.count {
                it == EstadoTrabajo.APROBADO || it == EstadoTrabajo.ACEPTADO_PENDIENTE_PAGO
            },
            rechazados = estados.count { it == EstadoTrabajo.RECHAZADO },
            pendientesDePago = estados.count { it == EstadoTrabajo.ACEPTADO_PENDIENTE_PAGO },
            sinProgramar = trabajos.count {
                it.estadoEnum() == EstadoTrabajo.APROBADO && it.id !in programados
            },
            presentaciones = presentaciones,
            resueltos = resueltos
        )
    }

    private fun TrabajoFirebase.estadoEnum(): EstadoTrabajo =
        runCatching { EstadoTrabajo.valueOf(estado) }
            .getOrDefault(EstadoTrabajo.ENVIADO)

    private fun CharlaFirebase.aPresentacion(): Presentacion? {
        val fecha = fecha?.aLocalDate() ?: return null
        val desde = runCatching { LocalTime.parse(horaInicio) }.getOrNull() ?: return null
        val hasta = runCatching { LocalTime.parse(horaFin) }.getOrNull() ?: return null
        val simposio = simposios.firstOrNull { it.id == simposioId }
        return Presentacion(
            charlaId = id,
            titulo = titulo,
            fecha = fecha,
            horaInicio = desde,
            horaFin = hasta,
            aula = simposio?.aulaNombre?.takeIf { it.isNotBlank() },
            simposio = simposio?.titulo
        )
    }

    private fun Timestamp.aLocalDate(): LocalDate =
        toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
    }
}

/** Estados que cuentan como "ya resuelto" por la organizacion. */
private val RESUELTOS = setOf(
    EstadoTrabajo.APROBADO,
    EstadoTrabajo.ACEPTADO_PENDIENTE_PAGO,
    EstadoTrabajo.RECHAZADO
)
