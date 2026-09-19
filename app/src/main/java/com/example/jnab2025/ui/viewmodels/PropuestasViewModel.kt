package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.data.model.TrabajoFirebase
import com.example.jnab2025.utils.Sesion
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.data.model.InscripcionFirebase
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.EstadoTrabajo
import com.google.firebase.firestore.WriteBatch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class PropuestasViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private var listenerPropuestas: ListenerRegistration? = null
    private val _simposio = MutableStateFlow<SimposioFirebase?>(null)
    val simposio: StateFlow<SimposioFirebase?> = _simposio.asStateFlow()
    private val _trabajo = MutableStateFlow<TrabajoFirebase?>(null)
    val trabajo: StateFlow<TrabajoFirebase?> = _trabajo.asStateFlow()

    private val _propuestas = MutableStateFlow<List<TrabajoFirebase>>(emptyList())
    val propuestas: StateFlow<List<TrabajoFirebase>> = _propuestas.asStateFlow()
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()
    private val _resueltas = Channel<Unit>(Channel.BUFFERED)
    val resueltas: Flow<Unit> = _resueltas.receiveAsFlow()
    // true mientras se aprueba o rechaza una propuesta
    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()
    fun cargarSimposio(simposioId: String) {

        firestore
            .collection("simposios")
            .document(simposioId)
            .get()
            .addOnSuccessListener { documento ->
                _simposio.value =
                    documento.toObject(
                        SimposioFirebase::class.java
                    )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo cargar el simposio: ${error.message}"
                )
            }
        escucharPropuestas(simposioId)
    }
    private fun escucharPropuestas(
        simposioId: String
    ) {
        listenerPropuestas?.remove()
        listenerPropuestas = firestore
            .collection("trabajos")
            .whereEqualTo(
                "simposioId",
                simposioId
            )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _avisos.trySend(
                        "No se pudieron cargar las propuestas: ${error.message}"
                    )
                    _propuestas.value = emptyList()
                    return@addSnapshotListener
                }
                val lista = snapshot
                    ?.documents
                    ?.mapNotNull { documento -> documento.toObject(
                            TrabajoFirebase::class.java
                        )
                    }
                    ?.filter { it.estado == EstadoTrabajo.ENVIADO.name
                    }
                    .orEmpty()
                _propuestas.value = lista
            }
    }
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
                _trabajo.value = trabajo
                trabajo?.let {
                    cargarSimposioDelTrabajo(
                        it.simposioId
                    )
                }
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo cargar el trabajo: ${error.message}"
                )
            }
    }
    private fun cargarSimposioDelTrabajo(
        simposioId: String
    ) {
        firestore
            .collection("simposios")
            .document(simposioId)
            .get()
            .addOnSuccessListener { documento ->
                _simposio.value =
                    documento.toObject(
                        SimposioFirebase::class.java
                    )
            }
    }
    fun rechazar(
        trabajoId: String,
        motivo: String
    ) {
        if (motivo.isBlank()) {
            _avisos.trySend(
                "Escribí el motivo del rechazo"
            )
            return
        }
        val organizadorUid = Sesion.firebaseUid(
                getApplication()
            )
        if (organizadorUid == null) {
            _avisos.trySend(
                "No hay un organizador autenticado"
            )
            return
        }
        val cambios = mapOf(
            "estado" to "RECHAZADO",
            "motivoRechazo" to motivo.trim(),
            "fechaResolucion" to Timestamp.now(),
            "resueltoPorUid" to organizadorUid
        )
        _enviando.value = true
        firestore
            .collection("trabajos")
            .document(trabajoId)
            .update(cambios)
            .addOnSuccessListener {
                terminar(
                    "Propuesta rechazada"
                )
                _resueltas.trySend(Unit)
            }
            .addOnFailureListener { error ->
                terminar(
                    "No se pudo rechazar: ${error.message}"
                )
            }
    }

    // cierra una resolución: apaga la ruedita y avisa el resultado
    private fun terminar(mensaje: String) {
        _enviando.value = false
        _avisos.trySend(mensaje)
    }
    fun aprobar(
        trabajoId: String
    ) {
        val organizadorUid =
            Sesion.firebaseUid(getApplication())

        if (organizadorUid == null) {
            _avisos.trySend(
                "No hay un organizador autenticado"
            )
            return
        }
        _enviando.value = true
        firestore
            .collection("trabajos")
            .document(trabajoId)
            .get()
            .addOnSuccessListener { documentoTrabajo ->

                val trabajo =
                    documentoTrabajo.toObject(
                        TrabajoFirebase::class.java
                    )

                if (trabajo == null) {
                    terminar(
                        "No se encontró el trabajo"
                    )
                    return@addOnSuccessListener
                }

                if (trabajo.estado != EstadoTrabajo.ENVIADO.name) {
                    terminar(
                        "Este trabajo ya fue resuelto"
                    )
                    return@addOnSuccessListener
                }

                verificarInscripcionParaAceptacion(
                    trabajo = trabajo,
                    organizadorUid = organizadorUid
                )
            }
            .addOnFailureListener { error ->

                terminar(
                    "No se pudo cargar el trabajo: ${error.message}"
                )
            }
    }
    private fun verificarInscripcionParaAceptacion(
        trabajo: TrabajoFirebase,
        organizadorUid: String
    ) {

        firestore
            .collection("inscripciones")
            .document(trabajo.autorUid)
            .get()
            .addOnSuccessListener { documento ->

                val inscripcion =
                    documento.toObject(
                        InscripcionFirebase::class.java
                    )

                /*la aceptación académica ocurre independientemente del pago
                 * Si todavía no está acreditado, queda pendiente explícitamente*/
                if (
                    inscripcion == null ||
                    inscripcion.estado != EstadoInscripcion.PAGADA.name
                ) {
                    guardarAceptacionPendientePago(
                        trabajo = trabajo,
                        organizadorUid = organizadorUid
                    )

                } else {
                    guardarAceptacionConPagoAcreditado(
                        trabajo = trabajo,
                        organizadorUid = organizadorUid
                    )
                }
            }
            .addOnFailureListener { error ->

                terminar(
                    "No se pudo verificar la inscripción: ${error.message}"
                )
            }
    }
    private fun cargarSimposioParaAprobar(
        trabajo: TrabajoFirebase,
        fecha: LocalDate,
        horaInicio: LocalTime,
        organizadorUid: String
    ) {
        firestore
            .collection("simposios")
            .document(trabajo.simposioId)
            .get()
            .addOnSuccessListener { documento ->
                val simposio = documento.toObject(
                    SimposioFirebase::class.java
                )
                if (simposio == null) {
                    _avisos.trySend(
                        "No se encontró el simposio"
                    )
                    return@addOnSuccessListener
                }
                val fechaInicioSimposio =
                    simposio.fechaInicio
                        ?.toDate()
                        ?.toInstant()
                        ?.atZone(ZoneId.systemDefault())
                        ?.toLocalDate()
                val fechaFinSimposio =
                    simposio.fechaFin
                        ?.toDate()
                        ?.toInstant()
                        ?.atZone(ZoneId.systemDefault())
                        ?.toLocalDate()
                if (
                    fechaInicioSimposio == null ||
                    fechaFinSimposio == null
                ) {
                    _avisos.trySend(
                        "El simposio no tiene fechas válidas"
                    )
                    return@addOnSuccessListener
                }
                if (
                    fecha < fechaInicioSimposio || fecha > fechaFinSimposio
                ) {
                    _avisos.trySend(
                        "La fecha debe estar entre " + "$fechaInicioSimposio y $fechaFinSimposio"
                    )
                    return@addOnSuccessListener
                }
                verificarHorarioDisponible(
                    trabajo = trabajo,
                    simposio = simposio,
                    fecha = fecha,
                    horaInicio = horaInicio,
                    organizadorUid = organizadorUid
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo cargar el simposio: ${error.message}"
                )
            }
    }
    private fun verificarHorarioDisponible(
        trabajo: TrabajoFirebase,
        simposio: SimposioFirebase,
        fecha: LocalDate,
        horaInicio: LocalTime,
        organizadorUid: String
    ) {
        val horaFin = horaInicio.plusMinutes(
            CharlaFirebase.MINUTOS_PRESENTACION.toLong()
        )
        // Se filtra por aula en el servidor: solo pueden chocar las charlas de
        // la misma sala. Traer la coleccion entera hacia que cada aprobacion
        // costara una lectura por charla del congreso.
        firestore
            .collection("charlas")
            .whereEqualTo("aulaId", simposio.aulaId)
            .get()
            .addOnSuccessListener { snapshot ->
                val charlas = snapshot.documents.mapNotNull {
                    it.toObject(CharlaFirebase::class.java)
                }
                val hayConflicto = charlas.any { charla ->
                    val fechaCharla =
                        charla.fecha
                            ?.toDate()
                            ?.toInstant()
                            ?.atZone(ZoneId.systemDefault())
                            ?.toLocalDate()
                    if (fechaCharla != fecha) {
                        false
                    } else {
                        val inicioExistente =
                            runCatching {
                                LocalTime.parse(charla.horaInicio)
                            }.getOrNull()
                        val finExistente =
                            runCatching {
                                LocalTime.parse(charla.horaFin)
                            }.getOrNull()
                        if (
                            inicioExistente == null ||
                            finExistente == null
                        ) {
                            false
                        } else {
                            val mismaAula =
                                charla.aulaId == simposio.aulaId
                            val seSuperponen =
                                horaInicio < finExistente && horaFin > inicioExistente
                            mismaAula && seSuperponen
                        }
                    }
                }
                if (hayConflicto) {
                    _avisos.trySend(
                        "Ya existe una actividad en esa aula y horario"
                    )
                    return@addOnSuccessListener
                }
                guardarAprobacion(
                    trabajo = trabajo,
                    simposio = simposio,
                    fecha = fecha,
                    horaInicio = horaInicio,
                    horaFin = horaFin,
                    organizadorUid = organizadorUid
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo verificar el horario: ${error.message}"
                )
            }
    }
    private fun guardarAceptacionPendientePago(
        trabajo: TrabajoFirebase,
        organizadorUid: String
    ) {
        val cambios = mapOf(
            "estado" to EstadoTrabajo.ACEPTADO_PENDIENTE_PAGO.name,
            "motivoRechazo" to null,
            "fechaResolucion" to Timestamp.now(),
            "resueltoPorUid" to organizadorUid
        )
        firestore
            .collection("trabajos")
            .document(trabajo.id)
            .update(cambios)
            .addOnSuccessListener {
                terminar(
                    "Trabajo aceptado académicamente. Falta acreditar la inscripción del expositor."
                )
                _resueltas.trySend(Unit)
            }
            .addOnFailureListener { error ->
                terminar("No se pudo actualizar el trabajo: ${error.message}"
                )
            }
    }
    private fun guardarAceptacionConPagoAcreditado(
        trabajo: TrabajoFirebase,
        organizadorUid: String
    ) {

        val cambios = mapOf(
            "estado" to EstadoTrabajo.APROBADO.name,
            "motivoRechazo" to null,
            "fechaResolucion" to Timestamp.now(),
            "resueltoPorUid" to organizadorUid
        )

        firestore
            .collection("trabajos")
            .document(trabajo.id)
            .update(cambios)
            .addOnSuccessListener {

                terminar(
                    "Trabajo aceptado. La inscripción ya está acreditada. " +
                            "Ahora falta programar la presentación."
                )
                _resueltas.trySend(Unit)
            }
            .addOnFailureListener { error ->

                terminar(
                    "No se pudo aceptar el trabajo: ${error.message}"
                )
            }
    }
    private fun guardarAprobacion(
        trabajo: TrabajoFirebase,
        simposio: SimposioFirebase,
        fecha: LocalDate,
        horaInicio: LocalTime,
        horaFin: LocalTime,
        organizadorUid: String
    ) {
        val charlaRef = firestore
            .collection("charlas")
            .document()
        val trabajoRef = firestore
            .collection("trabajos")
            .document(trabajo.id)
        val charla = CharlaFirebase(
            id = charlaRef.id,
            eventoId = "",
            simposioId = simposio.id,
            trabajoId = trabajo.id,
            aulaId = simposio.aulaId,
            tipo = TipoActividad.PRESENTACION.name,
            titulo = trabajo.titulo,
            fecha = fecha.toTimestamp(),
            horaInicio = horaInicio.toString(),
            horaFin = horaFin.toString()
        )
        val cambiosTrabajo = mapOf(
            "estado" to EstadoTrabajo.APROBADO.name,
            "motivoRechazo" to null,
            "fechaResolucion" to Timestamp.now(),
            "resueltoPorUid" to organizadorUid
        )
        val batch = firestore.batch()
        // Crear la charla programada.
        batch.set(
            charlaRef,
            charla
        )
        // Actualizar el mismo trabajo enviado por el expositor.
        batch.update(
            trabajoRef,
            cambiosTrabajo
        )
        batch.commit()
            .addOnSuccessListener {
                _avisos.trySend(
                    "Propuesta aprobada y charla programada"
                )
                _resueltas.trySend(Unit)
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo aprobar la propuesta: ${error.message}"
                )
            }
    }
    private fun LocalDate.toTimestamp(): Timestamp {
        val instant = this
            .atStartOfDay(
                ZoneId.systemDefault()
            )
            .toInstant()

        return Timestamp(
            java.util.Date.from(instant)
        )
    }
    override fun onCleared() {
        super.onCleared()
        listenerPropuestas?.remove()
    }
}