package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.model.AgendaUsuarioFirebase
import com.example.jnab2025.data.model.ActividadFirebase
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import java.time.Duration
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
    private var listenerActividades: ListenerRegistration? = null
    private var listenerAuth: FirebaseAuth.AuthStateListener? = null

    private var charlasFirebase: List<CharlaFirebase> = emptyList()
    private var trabajosFirebase: List<TrabajoFirebase> = emptyList()
    private var simposiosFirebase: List<SimposioFirebase> = emptyList()
    private var actividadesFirebase: List<ActividadFirebase> = emptyList()

    private var agendaCharlaIds: Set<String> = emptySet()
    private var agendaActividadIds: Set<String> = emptySet()

    /** Último conjunto de recordatorios programado. */
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
    private val _todos =
        MutableStateFlow<List<ItemAgendaFirebase>>(emptyList())

    /* Agenda personal del día: tanto una charla como una actividad pueden formar parte de Mi Agenda */
    val miAgendaDelDia: StateFlow<List<ItemAgendaFirebase>> =
        combine(_todos, _dia) { todos, dia ->

            if (dia == null) {
                emptyList()
            } else {
                todos.filter { item ->
                    item.fecha == dia &&
                            item.enMiAgenda
                }
            }

        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    sealed interface FilaAgenda {
        val clave: String
        data class Actividad(
            val item: ItemAgendaFirebase
        ) : FilaAgenda {
            override val clave: String
                get() = "actividad:${item.id}"
        }

        data class Tramo(
            val desdeId: String,
            val hastaId: String,
            val minutosLibres: Long,
            val desdeAula: String?,
            val hastaAula: String?,
            val desdePiso: Int?,
            val hastaPiso: Int?
        ) : FilaAgenda {
            override val clave: String
                get() = "tramo:$desdeId>$hastaId"
            val seSuperpone: Boolean
                get() = minutosLibres < 0
            val mismoLugar: Boolean
                get() =
                    desdeAula != null &&
                            desdeAula == hastaAula

            val cambiaDePiso: Boolean
                get() =
                    desdePiso != null &&
                            hastaPiso != null &&
                            desdePiso != hastaPiso
            val ajustado: Boolean
                get() =
                    !seSuperpone &&
                            !mismoLugar &&
                            minutosLibres <= 10
        }
    }

    val itinerario: StateFlow<List<FilaAgenda>> =
        miAgendaDelDia
            .map { items ->
                construirItinerario(items)
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                emptyList()
            )

    private fun construirItinerario(
        items: List<ItemAgendaFirebase>
    ): List<FilaAgenda> {

        if (items.isEmpty()) {
            return emptyList()
        }

        val ordenados =
            items.sortedWith(
                compareBy<ItemAgendaFirebase> { it.horaInicio }
                    .thenBy { it.horaFin }
            )

        val filas = mutableListOf<FilaAgenda>()

        ordenados.forEachIndexed { indice, item ->
            if (indice > 0) {
                val anterior = ordenados[indice - 1]
                filas += FilaAgenda.Tramo(
                    desdeId = anterior.id,
                    hastaId = item.id,
                    minutosLibres =
                        Duration.between(
                            anterior.horaFin,
                            item.horaInicio
                        ).toMinutes(),
                    desdeAula = anterior.aula,
                    hastaAula = item.aula,
                    desdePiso = anterior.piso,
                    hastaPiso = item.piso
                )
            }
            filas += FilaAgenda.Actividad(item)
        }
        return filas
    }

    init {
        escucharCharlas()
        escucharTrabajos()
        escucharSimposios()
        escucharActividades()

        listenerAuth = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                escucharMiAgenda()
            } else {
                listenerAgenda?.remove()
                listenerAgenda = null
                agendaCharlaIds = emptySet()
                agendaActividadIds = emptySet()

                reconstruir()
            }
        }
        auth.addAuthStateListener(listenerAuth!!)
    }

    private fun escucharCharlas() {
        listenerCharlas?.remove()
        listenerCharlas =
            firestore
                .collection("charlas")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _avisos.trySend(
                            "No se pudieron cargar las charlas: ${error.message}"
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

                    actividadesFirebase =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento.toObject(
                                    ActividadFirebase::class.java
                                )
                            }
                            .orEmpty()

                    reconstruir()
                }
    }

    //escucha la agenda personal del usuario autenticado
    private fun escucharMiAgenda() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            agendaCharlaIds = emptySet()
            agendaActividadIds = emptySet()

            reconstruir()

            return
        }

        listenerAgenda?.remove()
        listenerAgenda =
            firestore
                .collection("agendaUsuarios")
                .whereEqualTo("usuarioUid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _avisos.trySend(
                            "No se pudo cargar tu agenda: ${error.message}"
                        )
                        return@addSnapshotListener
                    }
                    agendaCharlaIds =
                        snapshot
                            ?.documents
                            ?.mapNotNull {
                                it.getString("charlaId")
                            }
                            ?.filter {
                                it.isNotBlank()
                            }
                            ?.toSet()
                            .orEmpty()
                    agendaActividadIds =
                        snapshot
                            ?.documents
                            ?.mapNotNull {
                                it.getString("actividadId")
                            }
                            ?.filter {
                                it.isNotBlank()
                            }
                            ?.toSet()
                            .orEmpty()

                    reconstruir()
                }
    }

    private fun reconstruir() {
        val listaCharlas =
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
                    enMiAgenda = charla.id in agendaCharlaIds
                )
            }

        val listaActividades =
            actividadesFirebase.mapNotNull { actividad ->

                val fecha =
                    actividad.fecha
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
                            actividad.horaInicio
                        )
                    }.getOrNull()
                        ?: return@mapNotNull null

                val hasta =
                    runCatching {
                        LocalTime.parse(
                            actividad.horaFin
                        )
                    }.getOrNull()
                        ?: return@mapNotNull null

                val tipo =
                    runCatching {
                        TipoActividad.valueOf(
                            actividad.tipo
                        )
                    }.getOrDefault(
                        TipoActividad.OTRO
                    )

                ItemAgendaFirebase(
                    actividadId = actividad.id,
                    titulo = actividad.titulo,
                    tipo = tipo,
                    fecha = fecha,
                    horaInicio = desde,
                    horaFin = hasta,
                    aula = actividad.aulaNombre
                        .takeIf {
                            it.isNotBlank()
                        },
                    edificio = actividad.aulaEdificio
                        .takeIf {
                            it.isNotBlank()
                        },
                    piso = actividad.aulaPiso,
                    simposio = null,
                    expositor = null,
                    enMiAgenda =
                        actividad.id in agendaActividadIds
                )
            }

        val lista =
            (listaCharlas + listaActividades)
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
            _dia.value == null ||
            _dia.value !in diasDisponibles
        ) {
            _dia.value =
                diasDisponibles.firstOrNull()
        }
        _todos.value = lista
        programarRecordatorios(lista)

        aplicarFiltro(lista)
    }

    /**
     * Mantiene las alarmas de recordatorio alineadas
     * con la agenda del usuario.
     *
     * Las actividades no generan recordatorios.
     */
    private fun programarRecordatorios(
        lista: List<ItemAgendaFirebase>
    ) {

        if (charlasFirebase.isEmpty()) {
            return
        }

        val mias =
            lista.filter {
                it.enMiAgenda &&
                        !it.esActividad
            }

        val firma =
            mias.joinToString("|") {
                "${it.charlaId}@${it.fecha}T${it.horaInicio}"
            }

        if (firma == firmaRecordatorios) {
            return
        }

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
            _items.value = emptyList()
            return
        }

        var filtrados =
            lista.filter {
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

    /* agrega o elimina una charla/actividad de la agenda personal */
    fun alternarAgenda(
        item: ItemAgendaFirebase
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _avisos.trySend(
                "Iniciá sesión para armar tu agenda"
            )
            return
        }

        val documentoId =
            if (item.esActividad) {
                "${uid}_actividad_${item.actividadId}"
            } else {
                "${uid}_${item.charlaId}"
            }

        val ref =
            firestore
                .collection("agendaUsuarios")
                .document(documentoId)

        if (item.enMiAgenda) {

            ref.delete()
                .addOnSuccessListener {

                    _avisos.trySend(
                        "Quitada de tu agenda"
                    )
                }
                .addOnFailureListener { error ->

                    _avisos.trySend(
                        "No se pudo quitar de tu agenda: ${error.message}"
                    )
                }

            return
        }

        /* buscamos superposiciones con otros elementos que ya tenga guardados */
        val choques =
            _items.value.filter { otra ->

                otra.enMiAgenda &&
                        otra.id != item.id &&
                        otra.fecha == item.fecha &&
                        item.horaInicio < otra.horaFin &&
                        item.horaFin > otra.horaInicio
            }

        val agenda =
            if (item.esActividad) {

                AgendaUsuarioFirebase(
                    usuarioUid = uid,
                    charlaId = "",
                    actividadId = item.actividadId,
                    agregadoEn = Timestamp.now()
                )
            } else {
                AgendaUsuarioFirebase(
                    usuarioUid = uid,
                    charlaId = item.charlaId,
                    actividadId = "",
                    agregadoEn = Timestamp.now()
                )
            }
        ref.set(agenda)
            .addOnSuccessListener {
                if (choques.isEmpty()) {
                    _avisos.trySend(
                        "Agregada a tu agenda"
                    )
                } else {
                    val otra = choques.first()
                    _avisos.trySend(
                        "Agregada, pero se superpone con " +
                                "\"${otra.titulo}\" " +
                                "(${otra.horaInicio}-${otra.horaFin})"
                    )
                }
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo agregar a tu agenda: ${error.message}"
                )
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerCharlas?.remove()
        listenerTrabajos?.remove()
        listenerSimposios?.remove()
        listenerActividades?.remove()
        listenerAgenda?.remove()
        listenerAuth?.let {
            auth.removeAuthStateListener(it)
        }
    }
}