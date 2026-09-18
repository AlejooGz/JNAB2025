package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.model.AulaFirebase
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.utils.Sesion
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class SimposioFormViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val _aulas = MutableStateFlow<List<AulaFirebase>>(emptyList())
    val aulas: StateFlow<List<AulaFirebase>> = _aulas.asStateFlow()
    private val _simposio = MutableStateFlow<SimposioFirebase?>(null)
    val simposio: StateFlow<SimposioFirebase?> = _simposio.asStateFlow()
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()
    private val _guardados = Channel<Unit>(Channel.BUFFERED)
    val guardados: Flow<Unit> = _guardados.receiveAsFlow()

    init {
        cargarAulas()
    }

    private fun cargarAulas() {
        firestore
            .collection("aulas")
            .get()
            .addOnSuccessListener { resultado ->

                val lista = resultado.documents
                    .mapNotNull { documento ->
                        documento.toObject(
                            AulaFirebase::class.java
                        )
                    }

                _aulas.value = lista
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudieron cargar las aulas: ${error.message}"
                )
            }
    }
    /**
     * El simposio dura un solo dia, asi que recibe una sola fecha. Igual se
     * siguen guardando fechaInicio y fechaFin con el mismo valor, para no
     * romper los documentos que ya estan en Firestore ni la validacion de
     * choque de aulas, que compara rangos.
     */
    fun guardar(
        simposioId: String?,
        titulo: String,
        tema: String,
        descripcion: String,
        aulaId: String?,
        fecha: LocalDate?
    ) {
        val desde = fecha
        val hasta = fecha

        when {
            titulo.isBlank() -> {
                _avisos.trySend("Falta el título")
                return
            }
            tema.isBlank() -> {
                _avisos.trySend("Falta el tema central")
                return
            }
            descripcion.isBlank() -> {
                _avisos.trySend("Falta la descripción")
                return
            }
            aulaId == null -> {
                _avisos.trySend("Elegí un aula")
                return
            }
            desde == null || hasta == null -> {
                _avisos.trySend("Elegí el día del simposio")
                return
            }
        }
        val organizadorUid =
            Sesion.firebaseUid(getApplication())
        if (organizadorUid == null) {
            _avisos.trySend(
                "No hay un organizador autenticado"
            )
            return
        }
        val aula = _aulas.value.firstOrNull {
            it.id == aulaId
        }
        if (aula == null) {
            _avisos.trySend(
                "No se encontró el aula seleccionada"
            )
            return
        }
        if (simposioId == null) {
            verificarConflictoYGuardar(
                organizadorUid = organizadorUid,
                aula = aula,
                titulo = titulo,
                tema = tema,
                descripcion = descripcion,
                desde = desde,
                hasta = hasta
            )
        } else {
            verificarConflictoYActualizar(
                simposioId = simposioId,
                organizadorUid = organizadorUid,
                aula = aula,
                titulo = titulo,
                tema = tema,
                descripcion = descripcion,
                desde = desde,
                hasta = hasta
            )
        }
    }

    private fun verificarConflictoYGuardar(
        organizadorUid: String,
        aula: AulaFirebase,
        titulo: String,
        tema: String,
        descripcion: String,
        desde: LocalDate,
        hasta: LocalDate
    ) {
        firestore
            .collection("simposios")
            .whereEqualTo("aulaId", aula.id)
            .get()
            .addOnSuccessListener { resultado ->

                val conflicto = resultado.documents
                    .mapNotNull {
                        it.toObject(
                            SimposioFirebase::class.java
                        )
                    }
                    .any { existente ->

                        val inicioExistente =
                            existente.fechaInicio
                                ?.toDate()
                                ?.toInstant()
                                ?.atZone(
                                    ZoneId.systemDefault()
                                )
                                ?.toLocalDate()

                        val finExistente =
                            existente.fechaFin
                                ?.toDate()
                                ?.toInstant()
                                ?.atZone(
                                    ZoneId.systemDefault()
                                )
                                ?.toLocalDate()

                        if (
                            inicioExistente == null ||
                            finExistente == null
                        ) {
                            false
                        } else {
                            desde <= finExistente &&
                                    hasta >= inicioExistente
                        }
                    }

                if (conflicto) {
                    _avisos.trySend(
                        "Esa aula ya está ocupada por otro simposio en esas fechas"
                    )
                    return@addOnSuccessListener
                }
                crearSimposio(
                    organizadorUid = organizadorUid,
                    aula = aula,
                    titulo = titulo,
                    tema = tema,
                    descripcion = descripcion,
                    desde = desde,
                    hasta = hasta
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudieron verificar los horarios: ${error.message}"
                )
            }
    }

    private fun crearSimposio(
        organizadorUid: String,
        aula: AulaFirebase,
        titulo: String,
        tema: String,
        descripcion: String,
        desde: LocalDate,
        hasta: LocalDate
    ) {

        val ref = firestore
            .collection("simposios")
            .document()

        val simposio = SimposioFirebase(
            id = ref.id,
            organizadorUid = organizadorUid,

            aulaId = aula.id,
            aulaNombre = aula.nombre,
            aulaEdificio = aula.edificio,
            aulaPiso = aula.piso,

            titulo = titulo.trim(),
            descripcion = descripcion.trim(),
            temaCentral = tema.trim(),

            fechaInicio = desde.toTimestamp(),
            fechaFin = hasta.toTimestamp()
        )

        ref.set(simposio)
            .addOnSuccessListener {

                _avisos.trySend(
                    "Simposio creado"
                )

                _guardados.trySend(Unit)
            }
            .addOnFailureListener { error ->

                _avisos.trySend(
                    "No se pudo guardar: ${error.message}"
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
            Date.from(instant)
        )
    }

    fun cargar(simposioId: String) {
        firestore
            .collection("simposios")
            .document(simposioId)
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
                _simposio.value = simposio
            }
            .addOnFailureListener { error ->

                _avisos.trySend(
                    "No se pudo cargar el simposio: ${error.message}"
                )
            }
    }
    private fun verificarConflictoYActualizar(
        simposioId: String,
        organizadorUid: String,
        aula: AulaFirebase,
        titulo: String,
        tema: String,
        descripcion: String,
        desde: LocalDate,
        hasta: LocalDate
    ) {
        firestore
            .collection("simposios")
            .whereEqualTo("aulaId", aula.id)
            .get()
            .addOnSuccessListener { resultado ->

                val conflicto = resultado.documents
                    // No comparar el simposio consigo mismo
                    .filter { documento ->
                        documento.id != simposioId
                    }
                    .mapNotNull { documento ->
                        documento.toObject(
                            SimposioFirebase::class.java
                        )
                    }
                    .any { existente ->

                        val inicioExistente =
                            existente.fechaInicio
                                ?.toDate()
                                ?.toInstant()
                                ?.atZone(
                                    ZoneId.systemDefault()
                                )
                                ?.toLocalDate()

                        val finExistente =
                            existente.fechaFin
                                ?.toDate()
                                ?.toInstant()
                                ?.atZone(
                                    ZoneId.systemDefault()
                                )
                                ?.toLocalDate()

                        if (
                            inicioExistente == null ||
                            finExistente == null
                        ) {
                            false
                        } else {
                            desde <= finExistente &&
                                    hasta >= inicioExistente
                        }
                    }

                if (conflicto) {
                    _avisos.trySend(
                        "Esa aula ya está ocupada por otro simposio en esas fechas"
                    )
                    return@addOnSuccessListener
                }
                actualizarSimposio(
                    simposioId = simposioId,
                    organizadorUid = organizadorUid,
                    aula = aula,
                    titulo = titulo,
                    tema = tema,
                    descripcion = descripcion,
                    desde = desde,
                    hasta = hasta
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudieron verificar los horarios: ${error.message}"
                )
            }
    }
    private fun actualizarSimposio(
        simposioId: String,
        organizadorUid: String,
        aula: AulaFirebase,
        titulo: String,
        tema: String,
        descripcion: String,
        desde: LocalDate,
        hasta: LocalDate
    ) {

        val simposio = SimposioFirebase(
            id = simposioId,
            organizadorUid = organizadorUid,
            aulaId = aula.id,
            aulaNombre = aula.nombre,
            aulaEdificio = aula.edificio,
            aulaPiso = aula.piso,
            titulo = titulo.trim(),
            descripcion = descripcion.trim(),
            temaCentral = tema.trim(),
            fechaInicio = desde.toTimestamp(),
            fechaFin = hasta.toTimestamp()
        )

        firestore
            .collection("simposios")
            .document(simposioId)
            .set(simposio)
            .addOnSuccessListener {
                _avisos.trySend(
                    "Simposio actualizado"
                )
                _guardados.trySend(Unit)
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo actualizar: ${error.message}"
                )
            }
    }
}