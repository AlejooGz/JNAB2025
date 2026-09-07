package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.data.model.TrabajoFirebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class CharlaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val _charlas =
        MutableStateFlow<List<CharlaFirebase>>(emptyList())
    val charlas: StateFlow<List<CharlaFirebase>> = _charlas.asStateFlow()
    private val _trabajo = MutableStateFlow<TrabajoFirebase?>(null)
    val trabajo: StateFlow<TrabajoFirebase?> = _trabajo.asStateFlow()
    private val _simposio = MutableStateFlow<SimposioFirebase?>(null)
    val simposio: StateFlow<SimposioFirebase?> = _simposio.asStateFlow()
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()
    private val _programadas = Channel<Unit>(Channel.BUFFERED)
    val programadas: Flow<Unit> = _programadas.receiveAsFlow()
    private var listenerCharlas: ListenerRegistration? = null

    companion object {
        /** Franja del dia en la que se pueden ubicar presentaciones. */
        val HORA_APERTURA: LocalTime = LocalTime.of(8, 0)
        val HORA_CIERRE: LocalTime = LocalTime.of(20, 0)
    }

    /** Un bloque de 30 minutos del dia del simposio. */
    data class Slot(
        val inicio: LocalTime,
        val fin: LocalTime,
        /** Titulo de la actividad que lo ocupa, o null si esta libre. */
        val ocupadoPor: String? = null
    ) {
        val libre: Boolean get() = ocupadoPor == null
    }

    /** El dia del simposio: las charlas no se programan en otro. */
    val fechaDelSimposio: StateFlow<LocalDate?> =
        _simposio
            .map { it?.fechaInicio?.toLocalDate() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Los bloques del dia con su estado. Se recalcula solo cuando cambia el
     * cronograma, asi que si otro organizador programa algo mientras esta
     * pantalla esta abierta, el slot se marca ocupado al instante.
     */
    val slots: StateFlow<List<Slot>> =
        combine(_simposio, _charlas, _trabajo) { simposio, charlas, trabajo ->
            calcularSlots(simposio, charlas, trabajo)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun calcularSlots(
        simposio: SimposioFirebase?,
        charlas: List<CharlaFirebase>,
        trabajo: TrabajoFirebase?
    ): List<Slot> {

        simposio ?: return emptyList()
        val fecha = simposio.fechaInicio?.toLocalDate() ?: return emptyList()

        // Solo puede chocar lo que ocupa la misma aula ese dia. La charla del
        // propio trabajo no cuenta: si se esta reprogramando, su horario actual
        // tiene que seguir disponible.
        val ocupadas = charlas.filter { charla ->
            charla.aulaId == simposio.aulaId &&
                charla.fecha?.toLocalDate() == fecha &&
                (trabajo == null || charla.trabajoId != trabajo.id)
        }

        val duracion = CharlaFirebase.MINUTOS_PRESENTACION.toLong()
        val slots = mutableListOf<Slot>()
        var inicio = HORA_APERTURA

        while (!inicio.plusMinutes(duracion).isAfter(HORA_CIERRE)) {
            val fin = inicio.plusMinutes(duracion)

            val choque = ocupadas.firstOrNull { charla ->
                val desde = runCatching { LocalTime.parse(charla.horaInicio) }.getOrNull()
                val hasta = runCatching { LocalTime.parse(charla.horaFin) }.getOrNull()
                desde != null && hasta != null && inicio < hasta && desde < fin
            }

            slots += Slot(inicio, fin, choque?.titulo)
            inicio = fin
        }
        return slots
    }

    init {
        escucharCharlas()
    }

    //escucha el cronograma Firebase completo.
    private fun escucharCharlas() {
        listenerCharlas?.remove()
        listenerCharlas =
            firestore
                .collection("charlas")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _avisos.trySend(
                            "No se pudo cargar el cronograma: ${error.message}"
                        )
                        return@addSnapshotListener
                    }
                    _charlas.value =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento.toObject(
                                    CharlaFirebase::class.java
                                )
                            }
                            .orEmpty()
                }
    }

    //carga un trabajo aprobado y su simposio.
    fun cargarTrabajo(
        trabajoId: String
    ) {
        firestore
            .collection("trabajos")
            .document(trabajoId)
            .get()
            .addOnSuccessListener { documento ->
                val trabajo =
                    documento.toObject(
                        TrabajoFirebase::class.java
                    )

                if (trabajo == null) {
                    _avisos.trySend(
                        "No se encontró el trabajo"
                    )
                    return@addOnSuccessListener
                }
                _trabajo.value = trabajo
                cargarSimposio(
                    trabajo.simposioId
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo cargar el trabajo: ${error.message}"
                )
            }
    }
    private fun cargarSimposio(
        simposioId: String
    ) {
        firestore
            .collection("simposios")
            .document(simposioId)
            .get()
            .addOnSuccessListener { documento ->

                val simposio =
                    documento.toObject(
                        SimposioFirebase::class.java
                    )

                if (simposio == null) {
                    _avisos.trySend(
                        "No se encontró el simposio"
                    )
                    return@addOnSuccessListener
                }
                _simposio.value = simposio
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo cargar el simposio: ${error.message}"
                )
            }
    }

    /**Programa un trabajo que ya fue:
     * 1.aceptado académicamente
     * 2.tiene inscripción acreditada
     * recién en este punto se convierte en CharlaFirebase.*/
    fun programarPresentacion(
        trabajoId: String,
        fecha: LocalDate,
        horaInicio: LocalTime
    ) {

        firestore
            .collection("trabajos")
            .document(trabajoId)
            .get()
            .addOnSuccessListener { documento ->
                val trabajo =
                    documento.toObject(
                        TrabajoFirebase::class.java
                    )

                if (trabajo == null) {
                    _avisos.trySend(
                        "No se encontró el trabajo"
                    )
                    return@addOnSuccessListener
                }

                if (
                    trabajo.estado !=
                    EstadoTrabajo.APROBADO.name
                ) {
                    _avisos.trySend(
                        "El trabajo todavía no está habilitado para programarse"
                    )
                    return@addOnSuccessListener
                }

                verificarSiYaEstaProgramado(
                    trabajo = trabajo,
                    fecha = fecha,
                    horaInicio = horaInicio
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo cargar el trabajo: ${error.message}"
                )
            }
    }
    private fun verificarSiYaEstaProgramado(
        trabajo: TrabajoFirebase,
        fecha: LocalDate,
        horaInicio: LocalTime
    ) {
        firestore
            .collection("charlas")
            .whereEqualTo(
                "trabajoId",
                trabajo.id
            )
            .get()
            .addOnSuccessListener { snapshot ->

                if (!snapshot.isEmpty) {
                    _avisos.trySend(
                        "Este trabajo ya tiene una presentación programada"
                    )
                    return@addOnSuccessListener
                }

                cargarSimposioParaProgramar(
                    trabajo = trabajo,
                    fecha = fecha,
                    horaInicio = horaInicio
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo comprobar la programación: ${error.message}"
                )
            }
    }

    private fun cargarSimposioParaProgramar(
        trabajo: TrabajoFirebase,
        fecha: LocalDate,
        horaInicio: LocalTime
    ) {
        firestore
            .collection("simposios")
            .document(trabajo.simposioId)
            .get()
            .addOnSuccessListener { documento ->

                val simposio =
                    documento.toObject(
                        SimposioFirebase::class.java
                    )

                if (simposio == null) {
                    _avisos.trySend(
                        "No se encontró el simposio"
                    )
                    return@addOnSuccessListener
                }

                val desde =
                    simposio.fechaInicio
                        ?.toDate()
                        ?.toInstant()
                        ?.atZone(
                            ZoneId.systemDefault()
                        )
                        ?.toLocalDate()

                val hasta =
                    simposio.fechaFin
                        ?.toDate()
                        ?.toInstant()
                        ?.atZone(
                            ZoneId.systemDefault()
                        )
                        ?.toLocalDate()

                if (
                    desde == null ||
                    hasta == null
                ) {
                    _avisos.trySend(
                        "El simposio no tiene fechas válidas"
                    )
                    return@addOnSuccessListener
                }
                if (
                    fecha < desde || fecha > hasta
                ) {
                    _avisos.trySend(
                        "La fecha debe estar entre $desde y $hasta"
                    )
                    return@addOnSuccessListener
                }
                verificarConflicto(
                    trabajo = trabajo,
                    simposio = simposio,
                    fecha = fecha,
                    horaInicio = horaInicio
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo cargar el simposio: ${error.message}"
                )
            }
    }

    /**Hay conflicto cuando:
     * - es el mismo día
     * - es la misma aula
     * - los horarios se superponen
     * La misma hora en días diferentes es válida.*/
    private fun verificarConflicto(
        trabajo: TrabajoFirebase,
        simposio: SimposioFirebase,
        fecha: LocalDate,
        horaInicio: LocalTime
    ) {
        val horaFin =
            horaInicio.plusMinutes(
                CharlaFirebase
                    .MINUTOS_PRESENTACION
                    .toLong()
            )
        firestore
            .collection("charlas")
            .get()
            .addOnSuccessListener { snapshot ->
                val charlas =
                    snapshot.documents
                        .mapNotNull { documento ->
                            documento.toObject(
                                CharlaFirebase::class.java
                            )
                        }

                val hayConflicto =
                    charlas.any { charla ->
                        val fechaCharla =
                            charla.fecha
                                ?.toDate()
                                ?.toInstant()
                                ?.atZone(
                                    ZoneId.systemDefault()
                                )
                                ?.toLocalDate()

                        if (fechaCharla != fecha) {
                            false
                        } else {
                            val inicioExistente =
                                runCatching {
                                    LocalTime.parse(
                                        charla.horaInicio
                                    )
                                }.getOrNull()

                            val finExistente =
                                runCatching {
                                    LocalTime.parse(
                                        charla.horaFin
                                    )
                                }.getOrNull()

                            if (
                                inicioExistente == null ||
                                finExistente == null
                            ) {
                                false
                            } else {
                                val mismaAula =
                                    charla.aulaId ==
                                            simposio.aulaId

                                val seSuperponen =
                                    horaInicio < finExistente &&
                                            horaFin > inicioExistente
                                mismaAula &&
                                        seSuperponen
                            }
                        }
                    }

                if (hayConflicto) {
                    _avisos.trySend(
                        "Ya existe una actividad en esa aula y horario"
                    )
                    return@addOnSuccessListener
                }

                guardarPresentacion(
                    trabajo = trabajo,
                    simposio = simposio,
                    fecha = fecha,
                    horaInicio = horaInicio,
                    horaFin = horaFin
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo verificar el horario: ${error.message}"
                )
            }
    }

    private fun guardarPresentacion(
        trabajo: TrabajoFirebase,
        simposio: SimposioFirebase,
        fecha: LocalDate,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ) {
        val charlaRef =
            firestore
                .collection("charlas")
                .document()

        val charla =
            CharlaFirebase(
                id = charlaRef.id,
                eventoId = "",
                simposioId = simposio.id,
                trabajoId = trabajo.id,
                aulaId = simposio.aulaId,
                tipo = TipoActividad.PRESENTACION
                        .name,
                titulo = trabajo.titulo,
                fecha = fecha.toTimestamp(),
                horaInicio = horaInicio.toString(),
                horaFin = horaFin.toString()
            )
        charlaRef
            .set(charla)
            .addOnSuccessListener {
                _avisos.trySend(
                    "Presentación programada correctamente"
                )
                _programadas.trySend(Unit)
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo programar la presentación: ${error.message}"
                )
            }
    }

    private fun Timestamp.toLocalDate(): LocalDate =
        toDate()
            .toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

    private fun LocalDate.toTimestamp():
            Timestamp {

        val instant =
            atStartOfDay(
                ZoneId.systemDefault()
            ).toInstant()

        return Timestamp(
            java.util.Date.from(
                instant
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        listenerCharlas?.remove()
    }
}