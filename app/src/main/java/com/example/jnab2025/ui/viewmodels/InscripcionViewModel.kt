package com.example.jnab2025.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.CategoriaInscripcion
import com.example.jnab2025.data.model.ComprobanteFirebase
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.InscripcionFirebase
import com.example.jnab2025.data.model.TipoInscripcion
import com.example.jnab2025.utils.Sesion
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class InscripcionViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    companion object {
        const val MONTO_GENERAL = 25000.0
        const val MONTO_ESTUDIANTE = 12500.0
    }
    data class Vista(
        val inscripcion: InscripcionFirebase? = null,
        val comprobante: ComprobanteFirebase? = null
    )
    private val _vista = MutableStateFlow(Vista())
    val vista: StateFlow<Vista> = _vista.asStateFlow()
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()
    // true mientras se guarda la inscripción o se sube el comprobante
    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()
    private var listenerInscripcion: ListenerRegistration? = null
    private var listenerComprobante: ListenerRegistration? = null

    init {
        escucharInscripcion()
    }

    fun montoPara(
        categoria: CategoriaInscripcion
    ): Double {

        return when (categoria) {
            CategoriaInscripcion.ESTUDIANTE -> MONTO_ESTUDIANTE
            CategoriaInscripcion.GENERAL -> MONTO_GENERAL
        }
    }

    fun inscribirse(
        tipo: TipoInscripcion,
        categoria: CategoriaInscripcion
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _avisos.trySend(
                "Iniciá sesión para inscribirte"
            )
            return
        }
        if (_vista.value.inscripcion != null) {
            _avisos.trySend(
                "Ya tenés una inscripción registrada"
            )
            return
        }

        //como hay una inscripción por usuario, usamos el UID como ID del documento
        val inscripcionRef = firestore
            .collection("inscripciones")
            .document(uid)

        val context =
            getApplication<Application>()

        val inscripcion =
            InscripcionFirebase(
                id = inscripcionRef.id,
                usuarioUid = uid,
                usuarioNombre = Sesion.nombre(context),
                usuarioEmail = Sesion.email(context) ?: "",
                tipo = tipo.name,
                categoria = categoria.name,
                estado = EstadoInscripcion
                        .PENDIENTE_PAGO
                        .name,
                monto = montoPara(categoria),
                fechaAlta = Timestamp.now()
            )
        _enviando.value = true
        inscripcionRef
            .set(inscripcion)
            .addOnSuccessListener {
                terminar(
                    "Inscripción registrada. Ahora cargá el comprobante de pago"
                )
            }
            .addOnFailureListener { error ->
                terminar(
                    "No se pudo registrar la inscripción: ${error.message}"
                )
            }
    }

    // cierra un envío: apaga la ruedita y avisa el resultado
    private fun terminar(mensaje: String) {
        _enviando.value = false
        _avisos.trySend(mensaje)
    }

    private fun escucharInscripcion() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _vista.value = Vista()
            return
        }
        listenerInscripcion?.remove()
        listenerInscripcion = firestore
            .collection("inscripciones")
            .document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _avisos.trySend(
                        "No se pudo cargar la inscripción: ${error.message}"
                    )
                    return@addSnapshotListener
                }
                val inscripcion =
                    snapshot?.toObject(
                        InscripcionFirebase::class.java
                    )
                _vista.value =
                    _vista.value.copy(
                        inscripcion = inscripcion
                    )
                if (inscripcion != null) {
                    escucharComprobante(
                        inscripcion.id
                    )
                }
            }
    }
    private fun escucharComprobante(
        inscripcionId: String
    ) {
        listenerComprobante?.remove()
        listenerComprobante = firestore
            .collection("comprobantes")
            .document(inscripcionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _avisos.trySend(
                        "No se pudo cargar el comprobante: ${error.message}"
                    )
                    return@addSnapshotListener
                }
                val comprobante =
                    snapshot?.toObject(
                        ComprobanteFirebase::class.java
                    )
                _vista.value =
                    _vista.value.copy(
                        comprobante = comprobante
                    )
            }
    }
    fun cargarComprobante(
        archivoUri: Uri?,
        nombreArchivo: String?
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) { _avisos.trySend(
                "Iniciá sesión para cargar el comprobante"
            )
            return
        }

        val inscripcion = _vista.value.inscripcion
        if (inscripcion == null) {
            _avisos.trySend(
                "Primero inscribite al evento"
            )
            return
        }
        if (
            inscripcion.estado ==
            EstadoInscripcion.PAGADA.name
        ) {
            _avisos.trySend(
                "Tu inscripción ya está acreditada"
            )
            return
        }
        if (archivoUri == null) { _avisos.trySend(
                "Elegí el archivo del comprobante"
            )
            return
        }
        val nombreSeguro =
            (nombreArchivo ?: "comprobante")
                .replace("/", "_")

        /* un usuario tiene un comprobante activo
         * por inscripción.Si reemplaza el archivo, se sobrescribe.*/
        val archivoRef = storage
            .reference
            .child(
                "comprobantes/$uid/${inscripcion.id}/$nombreSeguro"
            )
        _enviando.value = true
        archivoRef
            .putFile(archivoUri)
            .addOnSuccessListener {
                archivoRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        guardarComprobante(
                            inscripcion = inscripcion,
                            uid = uid,
                            nombreArchivo = nombreSeguro,
                            archivoUrl =
                                downloadUri.toString()
                        )
                    }
                    .addOnFailureListener { error -> terminar(
                            "El archivo se subió, pero no se pudo obtener su URL: ${error.message}"
                        )
                    }
            }
            .addOnFailureListener { error -> terminar(
                    "No se pudo subir el comprobante: ${error.message}"
                )
            }
    }
    private fun guardarComprobante(
        inscripcion: InscripcionFirebase,
        uid: String,
        nombreArchivo: String,
        archivoUrl: String
    ) {
        /*también usamos el ID de inscripción como ID de comprobante.
         * esto facilitareemplazar uno rechazado*/
        val comprobanteRef =
            firestore
                .collection("comprobantes")
                .document(inscripcion.id)

        val comprobante =
            ComprobanteFirebase(
                id = comprobanteRef.id,
                inscripcionId = inscripcion.id,
                usuarioUid = uid,
                archivoUrl = archivoUrl,
                nombreArchivo = nombreArchivo,
                fechaCarga = Timestamp.now(),
                estado = EstadoComprobante
                        .PENDIENTE
                        .name,
                verificadoPorUid = null,
                fechaVerificacion = null,
                motivoRechazo = null
            )
        comprobanteRef
            .set(comprobante)
            .addOnSuccessListener {
                terminar(
                    "Comprobante enviado. La organización lo va a verificar"
                )
            }
            .addOnFailureListener { error -> terminar(
                    "El archivo se subió, pero no se pudo guardar el comprobante: ${error.message}"
                )
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerInscripcion?.remove()
        listenerComprobante?.remove()
    }
}