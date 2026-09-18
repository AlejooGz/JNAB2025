package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.AgendaUsuarioFirebase
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.ItemAgendaFirebase
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.data.model.TrabajoFirebase
import com.example.jnab2025.notificaciones.ProgramadorRecordatorios
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

class CronogramaViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerCharlas: ListenerRegistration? = null
    private var listenerTrabajos: ListenerRegistration? = null
    private var listenerSimposios: ListenerRegistration? = null
    private var listenerAgenda: ListenerRegistration? = null
    private var charlasFirebase: List<CharlaFirebase> = emptyList()
    private var trabajosFirebase: List<TrabajoFirebase> = emptyList()
    private var simposiosFirebase: List<SimposioFirebase> = emptyList()
    private var agendaIds: Set<String> = emptySet()
    /** Ultimo conjunto de recordatorios programado, para no reprogramar de gusto. */
    private var firmaRecordatorios: String? = null
    private val _dia = MutableStateFlow<LocalDate?>(null)
    val dia: StateFlow<LocalDate?> = _dia.asStateFlow()
    private val _dias = MutableStateFlow<List<LocalDate>>(emptyList())
    val dias: StateFlow<List<LocalDate>> = _dias.asStateFlow()
    private val _items = MutableStateFlow<List<ItemAgendaFirebase>>(emptyList())
    val items: StateFlow<List<ItemAgendaFirebase>> = _items.asStateFlow()
    private val _soloMiAgenda = MutableStateFlow(false)
    val soloMiAgenda: StateFlow<Boolean> = _soloMiAgenda.asStateFlow()
    private val _avisos = Channel<String>(Channel.BUFFERED)

    val avisos: Flow<String> = _avisos.receiveAsFlow()
    init {
        escucharCharlas()
        escucharTrabajos()
        escucharSimposios()
        escucharMiAgenda()
    }
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
                    charlasFirebase =
                        snapshot
                            ?.documents
                            ?.mapNotNull {
                                it.toObject(
                                    CharlaFirebase::class.java
                                )
                            }
                            .orEmpty()
                    reconstruir()
                }
    }
    private fun escucharTrabajos() {
        listenerTrabajos?.remove()

        listenerTrabajos =
            firestore
                .collection("trabajos")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _avisos.trySend(
                            "No se pudieron cargar los trabajos: ${error.message}"
                        )
                        return@addSnapshotListener
                    }
                    trabajosFirebase =
                        snapshot
                            ?.documents
                            ?.mapNotNull {
                                it.toObject(
                                    TrabajoFirebase::class.java
                                )
                            }
                            .orEmpty()
                    reconstruir()
                }
    }

    private fun escucharSimposios() {
        listenerSimposios?.remove()

        listenerSimposios =
            firestore
                .collection("simposios")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _avisos.trySend(
                            "No se pudieron cargar los simposios: ${error.message}"
                        )
                        return@addSnapshotListener
                    }
                    simposiosFirebase =
                        snapshot
                            ?.documents
                            ?.mapNotNull {
                                it.toObject(
                                    SimposioFirebase::class.java
                                )
                            }
                            .orEmpty()
                    reconstruir()
                }
    }

    private fun escucharMiAgenda() {
        val uid =
            auth.currentUser?.uid
        if (uid == null) {
            agendaIds = emptySet()
            reconstruir()
            return
        }
        listenerAgenda?.remove()
        listenerAgenda =
            firestore
                .collection("agendaUsuarios")
                .whereEqualTo(
                    "usuarioUid",
                    uid
                )
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _avisos.trySend(
                            "No se pudo cargar tu agenda: ${error.message}"
                        )
                        return@addSnapshotListener
                    }
                    agendaIds =
                        snapshot
                            ?.documents
                            ?.mapNotNull {
                                it.getString("charlaId")
                            }
                            ?.toSet()
                            .orEmpty()
                    reconstruir()
                }
    }

    private fun reconstruir() {
        val lista =
            charlasFirebase.mapNotNull { charla ->
                val fecha =
                    charla.fecha
                        ?.toDate()
                        ?.toInstant()
                        ?.atZone(
                            ZoneId.systemDefault()
                        )
                        ?.toLocalDate()
                        ?: return@mapNotNull null
                val desde =
                    runCatching {
                        LocalTime.parse(
                            charla.horaInicio
                        )
                    }.getOrNull()
                        ?: return@mapNotNull null
                val hasta =
                    runCatching {
                        LocalTime.parse(
                            charla.horaFin
                        )
                    }.getOrNull()
                        ?: return@mapNotNull null
                val trabajo =
                    charla.trabajoId
                        ?.let { trabajoId ->
                            trabajosFirebase
                                .firstOrNull {
                                    it.id == trabajoId
                                }
                        }
                val simposio =
                    charla.simposioId
                        ?.let { simposioId ->
                            simposiosFirebase
                                .firstOrNull {
                                    it.id == simposioId
                                }
                        }
                val tipo =
                    runCatching {
                        TipoActividad.valueOf(
                            charla.tipo
                        )
                    }.getOrDefault(
                        TipoActividad.OTRO
                    )
                ItemAgendaFirebase(
                    charlaId = charla.id,
                    titulo = charla.titulo,
                    tipo = tipo,
                    fecha = fecha,
                    horaInicio = desde,
                    horaFin = hasta,
                    aula = simposio?.aulaNombre,
                    edificio = simposio?.aulaEdificio,
                    piso = simposio?.aulaPiso,
                    simposio = simposio?.titulo,
                    expositor = trabajo?.autorNombre,
                    enMiAgenda = charla.id in agendaIds
                )
            }
                .sortedWith(
                    compareBy<ItemAgendaFirebase> {
                        it.fecha
                    }.thenBy {
                        it.horaInicio
                    }
                )
        val diasDisponibles =
            lista
                .map {
                    it.fecha
                }
                .distinct()
                .sorted()
        _dias.value = diasDisponibles
        if (
            _dia.value == null || _dia.value !in diasDisponibles
        ) {
            _dia.value = diasDisponibles.firstOrNull()
        }
        programarRecordatorios(lista)
        aplicarFiltro(lista)
    }

    /**
     * Mantiene las alarmas de recordatorio alineadas con la agenda del usuario.
     *
     * Se engancha aca porque reconstruir() ya corre ante cualquier cambio que
     * importe: que el usuario marque o desmarque una charla, o que el
     * organizador le mueva el horario.
     */
    private fun programarRecordatorios(
        lista: List<ItemAgendaFirebase>
    ) {
        /* Si todavia no llegaron las charlas no hay nada que decidir, y
         * reprogramar con la lista vacia borraria alarmas que siguen siendo
         * validas mientras los listeners estan cargando. */
        if (charlasFirebase.isEmpty()) return

        val mias = lista.filter { it.enMiAgenda }

        /* reconstruir() se dispara con cada snapshot de los cuatro listeners;
         * sin esta firma estariamos rehaciendo las mismas alarmas de mas. */
        val firma =
            mias.joinToString("|") {
                "${it.charlaId}@${it.fecha}T${it.horaInicio}"
            }
        if (firma == firmaRecordatorios) return
        firmaRecordatorios = firma

        ProgramadorRecordatorios.reprogramar(
            getApplication(),
            mias.map { item ->
                ProgramadorRecordatorios.Programable(
                    charlaId = item.charlaId,
                    titulo = item.titulo,
                    inicio = LocalDateTime.of(
                        item.fecha,
                        item.horaInicio
                    ),
                    lugar = item.aula
                )
            }
        )
    }
    private fun aplicarFiltro(
        lista: List<ItemAgendaFirebase>
    ) {
        val diaSeleccionado = _dia.value
        if (diaSeleccionado == null) {
            _items.value =
                emptyList()
            return
        }
        var filtrados = lista.filter {
                it.fecha == diaSeleccionado
            }
        if (_soloMiAgenda.value) {
            filtrados =
                filtrados.filter {
                    it.enMiAgenda
                }
        }
        _items.value = filtrados
    }
    fun seleccionarDia(
        dia: LocalDate
    ) {
        _dia.value = dia
        reconstruir()
    }
    fun alternarSoloMiAgenda() {
        _soloMiAgenda.value =
            !_soloMiAgenda.value
        reconstruir()
    }

    fun alternarAgenda(
        item: ItemAgendaFirebase
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) { _avisos.trySend(
                "Iniciá sesión para armar tu agenda"
            )
            return
        }
        val documentoId =
            "${uid}_${item.charlaId}"
        val ref =
            firestore
                .collection("agendaUsuarios")
                .document(documentoId)
        if (item.enMiAgenda) {
            ref.delete()
                .addOnSuccessListener { _avisos.trySend(
                        "Quitada de tu agenda"
                    )
                }
                .addOnFailureListener { error -> _avisos.trySend(
                        "No se pudo quitar de tu agenda: ${error.message}"
                    )
                }
            return
        }
        val choques = _items.value.filter { otra ->
                otra.enMiAgenda &&
                        otra.fecha == item.fecha &&
                        otra.charlaId != item.charlaId &&
                        item.horaInicio < otra.horaFin &&
                        item.horaFin > otra.horaInicio
            }
        val agenda =
            AgendaUsuarioFirebase(
                usuarioUid = uid,
                charlaId = item.charlaId,
                agregadoEn = Timestamp.now()
            )
        ref.set(agenda)
            .addOnSuccessListener {
                if (choques.isEmpty()) {
                    _avisos.trySend(
                        "Agregada a tu agenda"
                    )
                } else {
                    val otra = choques.first()
                    _avisos.trySend(
                        "Agregada, pero se superpone con \"${otra.titulo}\" " +
                                "(${otra.horaInicio}-${otra.horaFin})"
                    )
                }
            }
            .addOnFailureListener { error -> _avisos.trySend(
                    "No se pudo agregar a tu agenda: ${error.message}"
                )
            }
    }
    override fun onCleared() {
        super.onCleared()
        listenerCharlas?.remove()
        listenerTrabajos?.remove()
        listenerSimposios?.remove()
        listenerAgenda?.remove()
    }
}