package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.ActividadFirebase
import com.example.jnab2025.data.model.AulaFirebase
import com.example.jnab2025.data.model.CharlaFirebase
import com.google.firebase.Timestamp
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

class ActividadViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()

    // Datos
    private val _actividades =
        MutableStateFlow<List<ActividadFirebase>>(emptyList())
    val actividades: StateFlow<List<ActividadFirebase>> =
        _actividades.asStateFlow()
    private val _charlas =
        MutableStateFlow<List<CharlaFirebase>>(emptyList())
    private val _aulas =
        MutableStateFlow<List<AulaFirebase>>(emptyList())
    val aulas: StateFlow<List<AulaFirebase>> = _aulas.asStateFlow()
    private val _actividad = MutableStateFlow<ActividadFirebase?>(null)
    val actividad: StateFlow<ActividadFirebase?> = _actividad.asStateFlow()

    // Avisos
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()
    private val _guardados = Channel<Unit>(Channel.BUFFERED)
    val guardados: Flow<Unit> = _guardados.receiveAsFlow()

    // Listeners
    private var listenerActividades: ListenerRegistration? = null
    private var listenerCharlas: ListenerRegistration? = null

    // Grilla oficial
    companion object {
        /*la grilla oficial del evento trabaja con bloques de 30 minutos*/
        val HORA_APERTURA: LocalTime = LocalTime.of(8, 0)
        val HORA_CIERRE: LocalTime = LocalTime.of(20, 0)
        const val MINUTOS_SLOT = 30L
    }

    /* un bloque de 30 minutos */
    data class Slot(
        val inicio: LocalTime,
        val fin: LocalTime,
        val ocupadoPor: String? = null
    ) {
        val libre: Boolean
            get() = ocupadoPor == null
    }

    init {
        cargarAulas()
        escucharActividades()
        escucharCharlas()
    }

    private fun cargarAulas() {
        firestore
            .collection("aulas")
            .get()
            .addOnSuccessListener { resultado ->
                _aulas.value =
                    resultado.documents
                        .mapNotNull { documento ->
                            documento.toObject(
                                AulaFirebase::class.java
                            )
                        }
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudieron cargar las aulas: ${error.message}"
                )
            }
    }

    private fun escucharActividades() {
        listenerActividades?.remove()
        listenerActividades =
            firestore
                .collection("actividades")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _actividades.value = emptyList()
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

    private fun escucharCharlas() {
        listenerCharlas?.remove()
        listenerCharlas =
            firestore
                .collection("charlas")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _charlas.value = emptyList()
                        _avisos.trySend(
                            "No se pudieron cargar las charlas: ${error.message}"
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

    // Actividad
    fun cargarActividad(
        actividadId: String
    ) {
        if (actividadId.isBlank()) {
            return
        }
        firestore
            .collection("actividades")
            .document(actividadId)
            .get()
            .addOnSuccessListener { documento ->
                val actividad =
                    documento.toObject(
                        ActividadFirebase::class.java
                    )
                if (actividad == null) {
                    _avisos.trySend(
                        "No se encontró la actividad"
                    )
                    return@addOnSuccessListener
                }
                _actividad.value = actividad
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo cargar la actividad: ${error.message}"
                )
            }
    }

    // Slots

    /** devuelve los bloques oficiales de 30 minutos para una fecha y aula determinadas
     * Se consideran ocupados tanto:
     * - las charlas existentes
     * - las actividades existentes */
    fun calcularSlots(
        fecha: LocalDate?,
        aulaId: String?,
        duracionMinutos: Long,
        propiaActividadId: String? = null
    ): List<Slot> {

        if (fecha == null || aulaId.isNullOrBlank()) {
            return emptyList()
        }

        if (duracionMinutos <= 0) {
            return emptyList()
        }
        val ocupaciones = obtenerOcupaciones(
            fecha = fecha,
            aulaId = aulaId,
            propiaActividadId = propiaActividadId
        )
        val slots = mutableListOf<Slot>()
        var inicio = HORA_APERTURA
        while (true) {
            // Duración completa de la actividad
            val finActividad = inicio.plusMinutes(duracionMinutos)

            // No puede terminar después del cierre oficial
            if (finActividad.isAfter(HORA_CIERRE)) {
                break
            }

            /* el horario de inicio solamente está disponible si TODOS los bloques necesarios
            para la duración están libres */
            val conflicto = ocupaciones.firstOrNull { ocupacion ->
                inicio < ocupacion.fin &&
                        finActividad > ocupacion.inicio
            }
            slots += Slot(
                inicio = inicio,
                fin = inicio.plusMinutes(MINUTOS_SLOT),
                ocupadoPor = conflicto?.titulo
            )

            inicio = inicio.plusMinutes(MINUTOS_SLOT)
        }

        return slots
    }
    /** devuelve todas las ocupaciones que afectan al aula y fecha seleccionadas */
    private fun obtenerOcupaciones(
        fecha: LocalDate,
        aulaId: String,
        propiaActividadId: String?
    ): List<Ocupacion> {
        val ocupaciones = mutableListOf<Ocupacion>()

        // charlas
        _charlas.value
            .filter { charla ->

                charla.aulaId == aulaId &&
                        convertirFecha(charla.fecha) == fecha
            }
            .forEach { charla ->
                val inicio = parsearHora(charla.horaInicio)
                val fin = parsearHora(charla.horaFin)
                if (inicio != null && fin != null) {
                    ocupaciones += Ocupacion(
                        inicio = inicio,
                        fin = fin,
                        titulo = charla.titulo
                    )
                }
            }

        // actividades
        _actividades.value
            .filter { actividad ->
                actividad.aulaId == aulaId &&
                        convertirFecha(actividad.fecha) == fecha &&
                        actividad.id != propiaActividadId
            }
            .forEach { actividad ->
                val inicio = parsearHora(actividad.horaInicio)
                val fin = parsearHora(actividad.horaFin)
                if (inicio != null && fin != null) {
                    ocupaciones += Ocupacion(
                        inicio = inicio,
                        fin = fin,
                        titulo = actividad.titulo
                    )
                }
            }
        return ocupaciones
    }

    private data class Ocupacion(
        val inicio: LocalTime,
        val fin: LocalTime,
        val titulo: String
    )

    /** Comprueba si una actividad puede ocupar el horario indicado
     * Regla: mismo día + mismo aula + horarios superpuestos = conflicto
     */
    private fun hayConflicto(
        actividad: ActividadFirebase,
        propiaActividadId: String? = null
    ): String? {
        val fecha = convertirFecha(actividad.fecha)
                ?: return "La actividad no tiene una fecha válida"
        val inicio = parsearHora(actividad.horaInicio)
                ?: return "La actividad no tiene una hora de inicio válida"
        val fin = parsearHora(actividad.horaFin)
                ?: return "La actividad no tiene una hora de finalización válida"
        if (!fin.isAfter(inicio)) {
            return "El horario de la actividad no es válido"
        }

        val conflictoCharla = _charlas.value.firstOrNull { charla ->
                val fechaCharla = convertirFecha(charla.fecha)
                val inicioCharla = parsearHora(charla.horaInicio)
                val finCharla = parsearHora(charla.horaFin)
                fechaCharla == fecha &&
                        charla.aulaId == actividad.aulaId &&
                        inicioCharla != null &&
                        finCharla != null &&
                        inicio < finCharla &&
                        fin > inicioCharla
            }
        if (conflictoCharla != null) {
            return "El horario se superpone con \"${conflictoCharla.titulo}\""
        }

        val conflictoActividad =
            _actividades.value.firstOrNull { existente ->
                if (
                    existente.id.isNotBlank() &&
                    existente.id == propiaActividadId
                ) {
                    false
                } else {

                    val fechaExistente = convertirFecha(existente.fecha)
                    val inicioExistente = parsearHora(existente.horaInicio)
                    val finExistente = parsearHora(existente.horaFin)
                    fechaExistente == fecha &&
                            existente.aulaId == actividad.aulaId &&
                            inicioExistente != null &&
                            finExistente != null &&
                            inicio < finExistente &&
                            fin > inicioExistente
                }
            }

        if (conflictoActividad != null) {
            return "El horario se superpone con \"${conflictoActividad.titulo}\""
        }

        return null
    }

    fun crearActividad(
        actividad: ActividadFirebase
    ) {
        val conflicto = hayConflicto(actividad)
        if (conflicto != null) {
            _avisos.trySend(conflicto)
            return
        }

        val referencia =
            firestore
                .collection("actividades")
                .document()
        val actividadFinal = actividad.copy(
                id = referencia.id
            )
        referencia
            .set(actividadFinal)
            .addOnSuccessListener {
                _avisos.trySend(
                    "Actividad creada correctamente"
                )
                _guardados.trySend(Unit)
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo crear la actividad: ${error.message}"
                )
            }
    }

    fun actualizarActividad(
        actividad: ActividadFirebase
    ) {
        if (actividad.id.isBlank()) {
            _avisos.trySend(
                "La actividad no tiene un identificador válido"
            )
            return
        }
        val conflicto = hayConflicto(
                actividad = actividad,
                propiaActividadId = actividad.id
            )
        if (conflicto != null) {
            _avisos.trySend(conflicto)
            return
        }

        firestore
            .collection("actividades")
            .document(actividad.id)
            .set(actividad)
            .addOnSuccessListener {
                _avisos.trySend(
                    "Actividad actualizada correctamente"
                )
                _guardados.trySend(Unit)
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo actualizar la actividad: ${error.message}"
                )
            }
    }

    fun eliminarActividad(
        actividadId: String
    ) {
        if (actividadId.isBlank()) {
            _avisos.trySend(
                "La actividad no tiene un identificador válido"
            )
            return
        }

        firestore
            .collection("actividades")
            .document(actividadId)
            .delete()
            .addOnSuccessListener {
                _avisos.trySend(
                    "Actividad eliminada"
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo eliminar la actividad: ${error.message}"
                )
            }
    }

    private fun convertirFecha(
        timestamp: Timestamp?
    ): LocalDate? {

        return timestamp
            ?.toDate()
            ?.toInstant()
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()
    }
    private fun parsearHora(
        hora: String?
    ): LocalTime? {

        if (hora.isNullOrBlank()) {
            return null
        }
        return runCatching {
            LocalTime.parse(hora)
        }.getOrNull()
    }

    override fun onCleared() {
        super.onCleared()
        listenerActividades?.remove()
        listenerActividades = null
        listenerCharlas?.remove()
        listenerCharlas = null
    }
}