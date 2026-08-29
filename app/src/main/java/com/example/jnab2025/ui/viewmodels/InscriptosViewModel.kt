package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.ComprobanteFirebase
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.InscripcionFirebase
import com.example.jnab2025.data.model.InscriptoSeguimientoFirebase
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

class InscriptosViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val _inscriptos =
        MutableStateFlow<List<InscriptoSeguimientoFirebase>>(
            emptyList()
        )
    val inscriptos:
            StateFlow<List<InscriptoSeguimientoFirebase>> =
        _inscriptos.asStateFlow()
    private val _avisos = Channel<String>(Channel.BUFFERED)

    val avisos: Flow<String> = _avisos.receiveAsFlow()
    private var listenerInscripciones: ListenerRegistration? = null
    init {
        escucharInscripciones()
    }
    private fun escucharInscripciones() {
        listenerInscripciones?.remove()
        listenerInscripciones =
            firestore
                .collection("inscripciones")
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        _avisos.trySend(
                            "No se pudieron cargar los inscriptos: ${error.message}"
                        )
                        return@addSnapshotListener
                    }
                    val inscripciones = snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento.toObject(
                                    InscripcionFirebase::class.java
                                )
                            }
                            .orEmpty()
                    cargarComprobantes(inscripciones)
                    /*si una inscripción ya figura PAGADA,
                     * nos aseguramos de que sus trabajos
                     * académicamente aceptados queden APROBADOS  */
                    inscripciones
                        .filter {
                            it.estado == EstadoInscripcion.PAGADA.name
                        }
                        .forEach { inscripcion ->
                            if (inscripcion.usuarioUid.isNotBlank()) {
                                habilitarTrabajosAceptados(
                                    usuarioUid = inscripcion.usuarioUid,
                                    mostrarAviso = false
                                )
                            }
                        }
                }
    }

    private fun cargarComprobantes(
        inscripciones: List<InscripcionFirebase>
    ) {

        if (inscripciones.isEmpty()) {
            _inscriptos.value = emptyList()
            return
        }

        firestore
            .collection("comprobantes")
            .get()
            .addOnSuccessListener { snapshot ->

                val comprobantes =
                    snapshot.documents
                        .mapNotNull { documento ->

                            documento.toObject(
                                ComprobanteFirebase::class.java
                            )
                        }
                val porInscripcion =
                    comprobantes.associateBy {
                        it.inscripcionId
                    }
                _inscriptos.value =
                    inscripciones
                        .map { inscripcion ->

                            InscriptoSeguimientoFirebase(
                                inscripcion =
                                    inscripcion,

                                comprobante =
                                    porInscripcion[
                                        inscripcion.id
                                    ]
                            )
                        }
                        .sortedBy {
                            it.inscripcion.usuarioNombre
                                .lowercase()
                        }
            }
            .addOnFailureListener { error ->

                _avisos.trySend(
                    "No se pudieron cargar los comprobantes: ${error.message}"
                )
            }
    }
    fun verificar(
        seguimiento: InscriptoSeguimientoFirebase
    ) {
        val organizadorUid = auth.currentUser?.uid
        if (organizadorUid == null) { _avisos.trySend(
                "No se pudo identificar al organizador"
            )
            return
        }
        val comprobante = seguimiento.comprobante
        if (comprobante == null) { _avisos.trySend(
                "Este inscripto todavía no cargó comprobante"
            )
            return
        }
        val comprobanteRef =
            firestore
                .collection("comprobantes")
                .document(comprobante.id)
        val inscripcionRef =
            firestore
                .collection("inscripciones")
                .document(
                    seguimiento.inscripcion.id
                )
        firestore.runBatch { batch ->
            batch.update(
                comprobanteRef,
                mapOf(
                    "estado" to
                            EstadoComprobante.VERIFICADO.name,
                    "verificadoPorUid" to
                            organizadorUid,
                    "fechaVerificacion" to
                            Timestamp.now(),
                    "motivoRechazo" to null
                )
            )
            batch.update(
                inscripcionRef,
                "estado",
                EstadoInscripcion.PAGADA.name
            )
        }.addOnSuccessListener {
            /*si el pago ya quedó acreditado, habilitamos cualquier trabajo
             aceptado académicamente de este usuario. */
            habilitarTrabajosAceptados(
                usuarioUid =
                    seguimiento.inscripcion.usuarioUid
            )
        }.addOnFailureListener { error -> _avisos.trySend(
                "No se pudo verificar el pago: ${error.message}"
            )
        }
    }
    private fun habilitarTrabajosAceptados(
        usuarioUid: String,
        mostrarAviso: Boolean = true
    ) {
        firestore
            .collection("trabajos")
            .whereEqualTo(
                "autorUid",
                usuarioUid
            )
            .get()
            .addOnSuccessListener { snapshot ->

                val trabajosPendientes =
                    snapshot.documents.filter { documento ->
                        documento.getString("estado") ==
                                EstadoTrabajo
                                    .ACEPTADO_PENDIENTE_PAGO
                                    .name
                    }
                if (trabajosPendientes.isEmpty()) {
                    if (mostrarAviso) { _avisos.trySend(
                            "Pago verificado correctamente"
                        )
                    }
                    return@addOnSuccessListener
                }
                val batch = firestore.batch()

                trabajosPendientes.forEach { documento ->
                    batch.update(
                        documento.reference,
                        mapOf(
                            "estado" to
                                    EstadoTrabajo.APROBADO.name,
                            "fechaResolucion" to
                                    Timestamp.now()
                        )
                    )
                }
                batch.commit()
                    .addOnSuccessListener {
                        if (mostrarAviso) { _avisos.trySend(
                                "Pago verificado. Los trabajos aceptados quedaron listos para programar."
                            )
                        }
                    }
                    .addOnFailureListener { error -> _avisos.trySend(
                            "La inscripción está pagada, pero no se pudieron habilitar los trabajos: ${error.message}"
                        )
                    }
            }
            .addOnFailureListener { error -> _avisos.trySend(
                    "No se pudieron sincronizar los trabajos con la inscripción: ${error.message}"
                )
            }
    }
    fun rechazar(
        seguimiento: InscriptoSeguimientoFirebase
    ) {

        val organizadorUid =
            auth.currentUser?.uid

        if (organizadorUid == null) {
            _avisos.trySend(
                "No se pudo identificar al organizador"
            )
            return
        }

        val comprobante =
            seguimiento.comprobante

        if (comprobante == null) {
            _avisos.trySend(
                "Este inscripto todavía no cargó comprobante"
            )
            return
        }

        firestore
            .collection("comprobantes")
            .document(comprobante.id)
            .update(
                mapOf(
                    "estado" to
                            EstadoComprobante.RECHAZADO.name,

                    "verificadoPorUid" to
                            organizadorUid,

                    "fechaVerificacion" to
                            Timestamp.now(),

                    "motivoRechazo" to
                            "Comprobante no válido"
                )
            )
            .addOnSuccessListener {

                _avisos.trySend(
                    "Comprobante rechazado"
                )

                escucharInscripciones()
            }
            .addOnFailureListener { error ->
                _avisos.trySend(
                    "No se pudo rechazar el comprobante: ${error.message}"
                )
            }
    }
    override fun onCleared() {
        super.onCleared()
        listenerInscripciones?.remove()
    }
}