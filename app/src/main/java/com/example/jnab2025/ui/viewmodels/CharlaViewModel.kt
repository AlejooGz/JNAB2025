package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.ActividadFirebase
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.NotificacionFirebase
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.data.model.TipoNotificacion
import com.example.jnab2025.data.model.TrabajoFirebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.WriteBatch
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
import java.time.format.DateTimeFormatter
import java.util.Locale

class CharlaViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val _charlas =
        MutableStateFlow<List<CharlaFirebase>>(emptyList())
    val charlas: StateFlow<List<CharlaFirebase>> = _charlas.asStateFlow()
    private val _actividades = MutableStateFlow<List<ActividadFirebase>>(emptyList())
    val actividades: StateFlow<List<ActividadFirebase>> = _actividades.asStateFlow()
    private val _trabajo = MutableStateFlow<TrabajoFirebase?>(null)
    val trabajo: StateFlow<TrabajoFirebase?> = _trabajo.asStateFlow()
    private val _simposio = MutableStateFlow<SimposioFirebase?>(null)
    val simposio: StateFlow<SimposioFirebase?> = _simposio.asStateFlow()
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()
    private val _programadas = Channel<Unit>(Channel.BUFFERED)
    val programadas: Flow<Unit> = _programadas.receiveAsFlow()
    // true desde que se piden las validaciones hasta que la charla queda guardada o falla
    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()
    private var listenerCharlas: ListenerRegistration? = null
    private var listenerActividades: ListenerRegistration? = null

    companion object {
        /** Franja del dia en la que se pueden ubicar presentaciones */
        val HORA_APERTURA: LocalTime = LocalTime.of(8, 0)
        val HORA_CIERRE: LocalTime = LocalTime.of(20, 0)

        /** "viernes 24/10" en el aviso de presentacion programada. */
        private val FORMATO_DIA_AVISO: DateTimeFormatter =
            DateTimeFormatter.ofPattern("EEEE dd/MM", Locale.forLanguageTag("es-AR"))
    }
    /** Un bloque de 30 minutos del dia del simposio */
    data class Slot(
        val inicio: LocalTime,
        val fin: LocalTime,
        /** Titulo de la actividad que lo ocupa, o null si esta libre. */
        val ocupadoPor: String? = null
    ) {
        val libre: Boolean get() = ocupadoPor == null
    }
    private data class OcupacionSlot(
        val inicio: LocalTime,
        val fin: LocalTime,
        val titulo: String
    )

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
        combine(
            _simposio,
            _charlas,
            _actividades,
            _trabajo
        ) { simposio, charlas, actividades, trabajo ->

            calcularSlots(
                simposio = simposio,
                charlas = charlas,
                actividades = actividades,
                trabajo = trabajo
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    private fun calcularSlots(
        simposio: SimposioFirebase?,
        charlas: List<CharlaFirebase>,
        actividades: List<ActividadFirebase>,
        trabajo: TrabajoFirebase?
    ): List<Slot> {
        simposio ?: return emptyList()
        val fecha = simposio.fechaInicio?.toLocalDate() ?: return emptyList()
        /*
         * Solo nos interesan las ocupaciones:
         * - del mismo día
         * - de la misma aula
         *
         * La charla del propio trabajo no cuenta porque
         * estamos permitiendo reprogramarla.
         */
        val ocupacionesCharlas =
            charlas
                .filter { charla ->
                    charla.aulaId == simposio.aulaId &&
                            charla.fecha?.toLocalDate() == fecha &&
                            (trabajo == null || charla.trabajoId != trabajo.id)
                }
                .mapNotNull { charla ->
                    val inicio = runCatching {
                            LocalTime.parse(charla.horaInicio)
                        }.getOrNull()
                    val fin = runCatching {
                            LocalTime.parse(charla.horaFin)
                        }.getOrNull()

                    if (inicio != null && fin != null) {
                        OcupacionSlot(
                            inicio = inicio,
                            fin = fin,
                            titulo = charla.titulo
                        )
                    } else {
                        null
                    }
                }
        val ocupacionesActividades =
            actividades
                .filter { actividad ->
                    actividad.aulaId == simposio.aulaId &&
                            actividad.fecha?.toLocalDate() == fecha
                }
                .mapNotNull { actividad ->
                    val inicio = runCatching {
                            LocalTime.parse(actividad.horaInicio)
                        }.getOrNull()
                    val fin = runCatching {
                            LocalTime.parse(actividad.horaFin)
                        }.getOrNull()
                    if (inicio != null && fin != null) {
                        OcupacionSlot(
                            inicio = inicio,
                            fin = fin,
                            titulo = actividad.titulo
                        )
                    } else {
                        null
                    }
                }
        val ocupadas = ocupacionesCharlas + ocupacionesActividades
        val duracion = CharlaFirebase.MINUTOS_PRESENTACION.toLong()
        val slots = mutableListOf<Slot>()
        var inicio = HORA_APERTURA
        while (!inicio.plusMinutes(duracion).isAfter(HORA_CIERRE)) {
            val fin = inicio.plusMinutes(duracion)
            val choque = ocupadas.firstOrNull { ocupacion ->

                    inicio < ocupacion.fin &&
                            fin > ocupacion.inicio
                }
            slots += Slot(
                inicio = inicio,
                fin = fin,
                ocupadoPor = choque?.titulo
            )
            inicio = fin
        }
        return slots
    }

    init {
        escucharCharlas()
        escucharActividades()
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
    private fun escucharActividades() {
        listenerActividades?.remove()
        listenerActividades =
            firestore
                .collection("actividades")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _avisos.trySend(
                            "No se pudieron cargar las actividades: ${error.message}"
                        )
                        return@addSnapshotListener
                    }
                    _actividades.value =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento.toObject(
                                    ActividadFirebase::class.java
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

        _enviando.value = true
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
                    terminar(
                        "No se encontró el trabajo"
                    )
                    return@addOnSuccessListener
                }

                if (
                    trabajo.estado !=
                    EstadoTrabajo.APROBADO.name
                ) {
                    terminar(
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
                terminar(
                    "No se pudo cargar el trabajo: ${error.message}"
                )
            }
    }

    // cierra la programación: apaga la ruedita y avisa el resultado
    private fun terminar(mensaje: String) {
        _enviando.value = false
        _avisos.trySend(mensaje)
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
                    terminar(
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
                terminar(
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
                    terminar(
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
                    terminar(
                        "El simposio no tiene fechas válidas"
                    )
                    return@addOnSuccessListener
                }
                if (
                    fecha < desde || fecha > hasta
                ) {
                    terminar(
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
                terminar(
                    "No se pudo cargar el simposio: ${error.message}"
                )
            }
    }

    private fun verificarConflicto(
        trabajo: TrabajoFirebase,
        simposio: SimposioFirebase,
        fecha: LocalDate,
        horaInicio: LocalTime
    ) {
        val horaFin =
            horaInicio.plusMinutes(
                CharlaFirebase.MINUTOS_PRESENTACION.toLong()
            )

        // primero obtenemos las charlas
        firestore
            .collection("charlas")
            .get()
            .addOnSuccessListener { snapshotCharlas ->
                val charlas =
                    snapshotCharlas.documents
                        .mapNotNull { documento ->
                            documento.toObject(
                                CharlaFirebase::class.java
                            )
                        }
                /* Buscamos conflicto unicamente entre: mismo día, misma aula, horarios superpuestos
                  La charla del propio trabajo se ignora porque ya comprobamos anteriormente que
                  no tenga una presentación programada */
                val conflictoCharla =
                    charlas.firstOrNull { charla ->

                        if (charla.trabajoId == trabajo.id) {
                            return@firstOrNull false
                        }
                        val fechaCharla =
                            charla.fecha
                                ?.toDate()
                                ?.toInstant()
                                ?.atZone(
                                    ZoneId.systemDefault()
                                )
                                ?.toLocalDate()
                        if (fechaCharla != fecha) {
                            return@firstOrNull false
                        }
                        if (charla.aulaId != simposio.aulaId) {
                            return@firstOrNull false
                        }
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
                            return@firstOrNull false
                        }
                        horaInicio < finExistente &&
                                horaFin > inicioExistente
                    }

                if (conflictoCharla != null) {
                    terminar(
                        "El horario se superpone con la charla \"${conflictoCharla.titulo}\""
                    )
                    return@addOnSuccessListener
                }
                // si no hubo conflicto con una charla, comprobamos las actividades
                firestore
                    .collection("actividades")
                    .get()
                    .addOnSuccessListener { snapshotActividades ->
                        val actividades =
                            snapshotActividades.documents
                                .mapNotNull { documento ->
                                    documento.toObject(
                                        ActividadFirebase::class.java
                                    )
                                }
                        val conflictoActividad =
                            actividades.firstOrNull { actividad ->
                                val fechaActividad =
                                    actividad.fecha
                                        ?.toDate()
                                        ?.toInstant()
                                        ?.atZone(
                                            ZoneId.systemDefault()
                                        )
                                        ?.toLocalDate()
                                if (fechaActividad != fecha) {
                                    return@firstOrNull false
                                }
                                if (
                                    actividad.aulaId !=
                                    simposio.aulaId
                                ) {
                                    return@firstOrNull false
                                }
                                val inicioExistente =
                                    runCatching {
                                        LocalTime.parse(
                                            actividad.horaInicio
                                        )
                                    }.getOrNull()
                                val finExistente =
                                    runCatching {
                                        LocalTime.parse(
                                            actividad.horaFin
                                        )
                                    }.getOrNull()
                                if (
                                    inicioExistente == null ||
                                    finExistente == null
                                ) {
                                    return@firstOrNull false
                                }
                                horaInicio < finExistente &&
                                        horaFin > inicioExistente
                            }
                        if (conflictoActividad != null) {
                            terminar(
                                "El horario se superpone con la actividad \"${conflictoActividad.titulo}\""
                            )
                            return@addOnSuccessListener
                        }
                        //no hay conflicto ni con charlas ni activvidades
                        guardarPresentacion(
                            trabajo = trabajo,
                            simposio = simposio,
                            fecha = fecha,
                            horaInicio = horaInicio,
                            horaFin = horaFin
                        )
                    }
                    .addOnFailureListener { error ->
                        terminar(
                            "No se pudieron verificar las actividades: ${error.message}"
                        )
                    }
            }
            .addOnFailureListener { error ->
                terminar(
                    "No se pudieron verificar las charlas: ${error.message}"
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
        // La charla y el aviso al autor van juntos: o se guardan las dos cosas
        // o ninguna, asi el expositor siempre se entera de su horario.
        val batch = firestore.batch()
        batch.set(charlaRef, charla)
        avisarAlAutor(batch, trabajo, simposio, charlaRef.id, fecha, horaInicio, horaFin)
        batch
            .commit()
            .addOnSuccessListener {
                terminar(
                    "Presentación programada correctamente"
                )
                _programadas.trySend(Unit)
            }
            .addOnFailureListener { error ->
                terminar(
                    "No se pudo programar la presentación: ${error.message}"
                )
            }
    }

    /**
     * Agrega al batch la notificacion PRESENTACION_PROGRAMADA para el autor
     * del trabajo, con el dia, el horario y el aula de su presentacion.
     */
    private fun avisarAlAutor(
        batch: WriteBatch,
        trabajo: TrabajoFirebase,
        simposio: SimposioFirebase,
        charlaId: String,
        fecha: LocalDate,
        horaInicio: LocalTime,
        horaFin: LocalTime
    ) {
        if (trabajo.autorUid.isBlank()) return

        val referencia = firestore
            .collection("notificaciones")
            .document()
        val mensaje = buildString {
            append("\"${trabajo.titulo}\" se presenta el ")
            append(fecha.format(FORMATO_DIA_AVISO))
            append(" de $horaInicio a $horaFin")
            if (simposio.aulaNombre.isNotBlank()) {
                append(" en ${simposio.aulaNombre}")
            }
            if (simposio.titulo.isNotBlank()) {
                append(" (${simposio.titulo})")
            }
            append(".")
        }

        batch.set(
            referencia,
            NotificacionFirebase(
                id = referencia.id,
                destinatarioUid = trabajo.autorUid,
                tipo = TipoNotificacion.PRESENTACION_PROGRAMADA.name,
                titulo = "Tu presentación ya tiene horario",
                mensaje = mensaje,
                // la charla, para abrir su detalle al tocarlo
                referenciaId = charlaId,
                creadaEn = Timestamp.now()
            )
        )
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
        listenerActividades?.remove()
    }
}