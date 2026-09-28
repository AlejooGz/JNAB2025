package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.data.model.TrabajoFirebase
import com.example.jnab2025.utils.Sesion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeOrganizadorViewModel(
    application: Application
) : AndroidViewModel(application) {
    data class Resumen(
        val cantidadSimposios: Int = 0,
        val propuestasPendientes: Int = 0,
        val pagosPendientes: Int = 0,
        val cantidadInscriptos: Int = 0,
        val trabajosSinProgramar: Int = 0,
        val cargando: Boolean = true
    )

    private val firestore = FirebaseFirestore.getInstance()
    private val _resumen = MutableStateFlow(Resumen())
    val resumen: StateFlow<Resumen> = _resumen.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _simposiosConPropuestas =
        MutableStateFlow<List<SimposioFirebase>>(emptyList())
    val simposiosConPropuestas: StateFlow<List<SimposioFirebase>> =
        _simposiosConPropuestas.asStateFlow()
    private val _simposiosConTrabajosSinProgramar =
        MutableStateFlow<List<SimposioFirebase>>(emptyList())
    val simposiosConTrabajosSinProgramar: StateFlow<List<SimposioFirebase>> =
        _simposiosConTrabajosSinProgramar.asStateFlow()

    private var listenerSimposios: ListenerRegistration? = null
    private var listenerTrabajosEnviados: ListenerRegistration? = null
    private var listenerTrabajosAceptadosPendientesPago: ListenerRegistration? = null
    private var listenerTrabajosAprobados: ListenerRegistration? = null
    private var listenerCharlas: ListenerRegistration? = null
    private var listenerComprobantes: ListenerRegistration? = null
    private var simposiosActuales: List<SimposioFirebase> = emptyList()
    private var trabajosEnviados: List<TrabajoFirebase> = emptyList()
    private var trabajosAceptadosPendientesPago: List<TrabajoFirebase> = emptyList()
    private var trabajosAprobados: List<TrabajoFirebase> = emptyList()
    private var idsCharlasProgramadas: Set<String> = emptySet()

    init {
        escucharSimposios()
        escucharTrabajosEnviados()
        escucharTrabajosAceptadosPendientesPago()
        escucharTrabajosAprobados()
        escucharCharlas()
        escucharComprobantes()
    }

    private fun escucharSimposios() {
        val organizadorUid = Sesion.firebaseUid(getApplication())

        if (organizadorUid == null) {
            _error.value = "No hay un organizador autenticado"
            _resumen.value =
                _resumen.value.copy(
                    cargando = false
                )

            return
        }
        listenerSimposios?.remove()
        listenerSimposios =
            firestore
                .collection("simposios")
                .whereEqualTo(
                    "organizadorUid",
                    organizadorUid
                )
                .addSnapshotListener {
                        snapshot,
                        error ->

                    if (error != null) {

                        _error.value =
                            "No se pudieron cargar los simposios: ${error.message}"

                        _resumen.value =
                            _resumen.value.copy(
                                cargando = false
                            )

                        return@addSnapshotListener
                    }

                    simposiosActuales =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento.toObject(
                                    SimposioFirebase::class.java
                                )
                            }
                            .orEmpty()

                    _resumen.value =
                        _resumen.value.copy(
                            cantidadSimposios =
                                simposiosActuales.size,
                            cargando = false
                        )
                    recalcularTodo()
                    _error.value = null
                }
    }

    private fun escucharTrabajosEnviados() {
        listenerTrabajosEnviados?.remove()
        listenerTrabajosEnviados =
            firestore
                .collection("trabajos")
                .whereEqualTo(
                    "estado",
                    EstadoTrabajo.ENVIADO.name
                )
                .addSnapshotListener {
                        snapshot,
                        error ->

                    if (error != null) {
                        _error.value =
                            "No se pudieron cargar las propuestas: ${error.message}"

                        return@addSnapshotListener
                    }
                    trabajosEnviados =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento.toObject(
                                    TrabajoFirebase::class.java
                                )
                            }
                            .orEmpty()
                    recalcularTodo()
                }
    }

    private fun escucharTrabajosAceptadosPendientesPago() {
        listenerTrabajosAceptadosPendientesPago?.remove()
        listenerTrabajosAceptadosPendientesPago =
            firestore
                .collection("trabajos")
                .whereEqualTo(
                    "estado",
                    EstadoTrabajo
                        .ACEPTADO_PENDIENTE_PAGO
                        .name
                )
                .addSnapshotListener {
                        snapshot,
                        error ->

                    if (error != null) {

                        _error.value =
                            "No se pudieron cargar los trabajos aceptados pendientes: ${error.message}"

                        return@addSnapshotListener
                    }

                    trabajosAceptadosPendientesPago =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento.toObject(
                                    TrabajoFirebase::class.java
                                )
                            }
                            .orEmpty()

                    recalcularTodo()
                }
    }

    private fun escucharTrabajosAprobados() {

        listenerTrabajosAprobados?.remove()

        listenerTrabajosAprobados =
            firestore
                .collection("trabajos")
                .whereEqualTo(
                    "estado",
                    EstadoTrabajo.APROBADO.name
                )
                .addSnapshotListener {
                        snapshot,
                        error ->

                    if (error != null) {

                        _error.value =
                            "No se pudieron cargar los trabajos aprobados: ${error.message}"

                        return@addSnapshotListener
                    }

                    trabajosAprobados =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento.toObject(
                                    TrabajoFirebase::class.java
                                )
                            }
                            .orEmpty()

                    recalcularTodo()
                }
    }

    private fun escucharCharlas() {

        listenerCharlas?.remove()

        listenerCharlas =
            firestore
                .collection("charlas")
                .addSnapshotListener {
                        snapshot,
                        error ->

                    if (error != null) {

                        _error.value =
                            "No se pudieron cargar las charlas programadas: ${error.message}"

                        return@addSnapshotListener
                    }

                    idsCharlasProgramadas =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento
                                    .getString("trabajoId")
                            }
                            ?.toSet()
                            .orEmpty()
                    recalcularTodo()
                }
    }

    private fun escucharComprobantes() {
        listenerComprobantes?.remove()
        listenerComprobantes =
            firestore
                .collection("comprobantes")
                .addSnapshotListener {
                        snapshot,
                        error ->
                    if (error != null) {
                        _error.value =
                            "No se pudieron cargar los comprobantes: ${error.message}"
                        return@addSnapshotListener
                    }
                    val documentos = snapshot?.documents.orEmpty()

                    val pendientes =
                        documentos.count { documento ->
                            documento.getString("estado") == "PENDIENTE"
                        }

                    val inscriptosVerificados =
                        documentos
                            .filter { documento ->
                                documento.getString("estado") ==
                                        "VERIFICADO"
                            }
                            .map { documento ->
                                documento.getString("inscripcionId")
                                    ?: documento.id
                            }
                            .toSet()
                            .size

                    _resumen.value =
                        _resumen.value.copy(
                            pagosPendientes = pendientes,
                            cantidadInscriptos =
                                inscriptosVerificados
                        )
                }
    }

    private fun recalcularTodo() {

        val idsSimposios =
            simposiosActuales
                .map { it.id }
                .toSet()

        val propuestas =
            trabajosEnviados
                .filter {
                    it.simposioId in idsSimposios
                }

        val trabajosSinProgramar =
            trabajosAprobados
                .filter {
                    it.simposioId in idsSimposios
                }
                .filter { trabajo ->
                    trabajo.id !in idsCharlasProgramadas
                }

        val aceptadosPendientesPago =
            trabajosAceptadosPendientesPago
                .count {
                    it.simposioId in idsSimposios
                }

        val simposiosConPropuestas =
            simposiosActuales
                .filter { simposio ->
                    propuestas.any { trabajo ->
                        trabajo.simposioId ==
                                simposio.id
                    }
                }
                .sortedBy {
                    it.titulo.lowercase()
                }

        val simposiosConTrabajosSinProgramar =
            simposiosActuales
                .filter { simposio ->
                    trabajosSinProgramar.any { trabajo ->
                        trabajo.simposioId ==
                                simposio.id
                    }
                }
                .sortedBy {
                    it.titulo.lowercase()
                }
        _resumen.value =
            _resumen.value.copy(
                propuestasPendientes = propuestas.size,
                trabajosSinProgramar = trabajosSinProgramar.size,
                pagosPendientes = _resumen.value.pagosPendientes,
                cantidadInscriptos = _resumen.value.cantidadInscriptos,
                cantidadSimposios = simposiosActuales.size,
                cargando = false
            )

        _simposiosConPropuestas.value = simposiosConPropuestas

        _simposiosConTrabajosSinProgramar.value = simposiosConTrabajosSinProgramar
    }

    override fun onCleared() {
        super.onCleared()
        listenerSimposios?.remove()
        listenerTrabajosEnviados?.remove()
        listenerTrabajosAceptadosPendientesPago?.remove()
        listenerTrabajosAprobados?.remove()
        listenerCharlas?.remove()
        listenerComprobantes?.remove()
    }
}