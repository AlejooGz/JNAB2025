package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.ComprobanteFirebase
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.InscripcionFirebase
import com.example.jnab2025.data.model.NovedadFirebase
import com.example.jnab2025.data.model.SimposioFirebase
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
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Datos del home del asistente (pantalla hecha en Compose).
 *
 * A diferencia de los ViewModels de las pantallas XML, expone un unico
 * [Estado] con todo lo que la pantalla muestra: Compose redibuja solo lo que
 * cambio, asi que alcanza con reemplazar el objeto entero en cada snapshot.
 *
 * El home del expositor tambien lo usa, para las secciones que comparten.
 */
class HomeAsistenteViewModel(
    application: Application
) : AndroidViewModel(application) {

    /** Una charla de Mi Agenda que todavia no termino. */
    data class CharlaProxima(
        val charlaId: String,
        val titulo: String,
        val fecha: LocalDate,
        val horaInicio: LocalTime,
        val horaFin: LocalTime,
        val aula: String?
    )

    data class NovedadResumen(
        val id: String,
        val titulo: String,
        val fecha: LocalDate?
    )

    /** Lo que el home le tiene que decir al usuario sobre su inscripcion. */
    sealed interface SituacionInscripcion {
        data object SinInscripcion : SituacionInscripcion
        data class FaltaPago(val monto: Double) : SituacionInscripcion
        data object ComprobanteEnRevision : SituacionInscripcion
        data class ComprobanteRechazado(val motivo: String?) : SituacionInscripcion
        data object Confirmada : SituacionInscripcion
        data object Anulada : SituacionInscripcion
    }

    data class Estado(
        val cargando: Boolean = true,
        /** Primer y ultimo dia con charlas: de ahi sale la cuenta regresiva. */
        val inicioJornadas: LocalDate? = null,
        val finJornadas: LocalDate? = null,
        val inscripcion: SituacionInscripcion = SituacionInscripcion.SinInscripcion,
        val proximasCharlas: List<CharlaProxima> = emptyList(),
        val novedades: List<NovedadResumen> = emptyList()
    )

    companion object {
        const val MAX_CHARLAS = 3
        const val MAX_NOVEDADES = 3
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().currentUser?.uid

    private val _estado = MutableStateFlow(Estado())
    val estado: StateFlow<Estado> = _estado.asStateFlow()

    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()

    private val listeners = mutableListOf<ListenerRegistration>()

    // ultimos datos recibidos de cada listener; reconstruir() los combina
    private var charlas: List<CharlaFirebase> = emptyList()
    private var simposios: List<SimposioFirebase> = emptyList()
    private var agendaIds: Set<String> = emptySet()
    private var inscripcion: InscripcionFirebase? = null
    private var comprobante: ComprobanteFirebase? = null

    /* El home sigue "cargando" hasta que respondieron los listeners de los
     * que depende lo principal; si no, mostraria "no tenes charlas" durante
     * el medio segundo en que todavia no llego nada. */
    private val pendientes = mutableSetOf("charlas", "agenda", "inscripcion")

    init {
        escucharCharlas()
        escucharSimposios()
        escucharNovedades()
        if (uid == null) {
            pendientes.clear()
            reconstruir()
        } else {
            escucharAgenda(uid)
            escucharInscripcion(uid)
            escucharComprobante(uid)
        }
    }

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

    private fun escucharAgenda(uid: String) {
        listeners += firestore
            .collection("agendaUsuarios")
            .whereEqualTo("usuarioUid", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _avisos.trySend("No se pudo cargar tu agenda: ${error.message}")
                } else {
                    agendaIds = snapshot
                        ?.documents
                        ?.mapNotNull { it.getString("charlaId") }
                        ?.toSet()
                        .orEmpty()
                }
                listo("agenda")
            }
    }

    // la inscripcion y su comprobante usan el UID como id de documento
    private fun escucharInscripcion(uid: String) {
        listeners += firestore
            .collection("inscripciones")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _avisos.trySend("No se pudo cargar tu inscripción: ${error.message}")
                } else {
                    inscripcion = snapshot?.toObject(InscripcionFirebase::class.java)
                }
                listo("inscripcion")
            }
    }

    private fun escucharComprobante(uid: String) {
        listeners += firestore
            .collection("comprobantes")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                comprobante = snapshot?.toObject(ComprobanteFirebase::class.java)
                reconstruir()
            }
    }

    private fun escucharNovedades() {
        listeners += firestore
            .collection("novedades")
            .whereEqualTo("publicada", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val novedades = snapshot
                    ?.documents
                    ?.mapNotNull { documento ->
                        documento
                            .toObject(NovedadFirebase::class.java)
                            ?.copy(id = documento.id)
                    }
                    .orEmpty()
                    .sortedByDescending { it.fechaPublicacion }
                    .take(MAX_NOVEDADES)
                    .map {
                        NovedadResumen(
                            id = it.id,
                            titulo = it.titulo,
                            fecha = it.fechaPublicacion?.aLocalDate()
                        )
                    }
                _estado.value = _estado.value.copy(novedades = novedades)
            }
    }

    private fun listo(listener: String) {
        pendientes.remove(listener)
        reconstruir()
    }

    private fun reconstruir() {
        val fechas = charlas.mapNotNull { it.fecha?.aLocalDate() }
        val ahora = LocalDateTime.now()

        val proximas = charlas
            .filter { it.id in agendaIds }
            .mapNotNull { charla -> charla.aProxima() }
            // una charla sigue siendo "proxima" hasta que termina
            .filter { LocalDateTime.of(it.fecha, it.horaFin).isAfter(ahora) }
            .sortedWith(compareBy({ it.fecha }, { it.horaInicio }))
            .take(MAX_CHARLAS)

        _estado.value = _estado.value.copy(
            cargando = pendientes.isNotEmpty(),
            inicioJornadas = fechas.minOrNull(),
            finJornadas = fechas.maxOrNull(),
            inscripcion = situacionInscripcion(),
            proximasCharlas = proximas
        )
    }

    private fun situacionInscripcion(): SituacionInscripcion {
        val inscripcion = inscripcion
            ?: return SituacionInscripcion.SinInscripcion

        return when (inscripcion.estado) {
            EstadoInscripcion.PAGADA.name -> SituacionInscripcion.Confirmada
            EstadoInscripcion.ANULADA.name -> SituacionInscripcion.Anulada
            else -> when (comprobante?.estado) {
                null -> SituacionInscripcion.FaltaPago(inscripcion.monto)
                EstadoComprobante.PENDIENTE.name ->
                    SituacionInscripcion.ComprobanteEnRevision
                EstadoComprobante.RECHAZADO.name ->
                    SituacionInscripcion.ComprobanteRechazado(comprobante?.motivoRechazo)
                // el comprobante y la inscripcion se verifican en el mismo
                // batch; si llega uno antes que el otro, ya esta confirmada
                else -> SituacionInscripcion.Confirmada
            }
        }
    }

    private fun CharlaFirebase.aProxima(): CharlaProxima? {
        val fecha = fecha?.aLocalDate() ?: return null
        val desde = runCatching { LocalTime.parse(horaInicio) }.getOrNull() ?: return null
        val hasta = runCatching { LocalTime.parse(horaFin) }.getOrNull() ?: return null
        val simposio = simposios.firstOrNull { it.id == simposioId }
        return CharlaProxima(
            charlaId = id,
            titulo = titulo,
            fecha = fecha,
            horaInicio = desde,
            horaFin = hasta,
            aula = simposio?.aulaNombre?.takeIf { it.isNotBlank() }
        )
    }

    private fun Timestamp.aLocalDate(): LocalDate =
        toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

    override fun onCleared() {
        super.onCleared()
        listeners.forEach { it.remove() }
    }
}
