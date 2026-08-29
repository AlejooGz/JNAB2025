package com.example.jnab2025.ui.viewmodels

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.data.model.TrabajoFirebase
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

class EnviarTrabajoViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _simposios =
        MutableStateFlow<List<SimposioFirebase>>(emptyList())

    val simposios: StateFlow<List<SimposioFirebase>> =
        _simposios.asStateFlow()

    private var listenerSimposios: ListenerRegistration? = null

    sealed interface Envio {
        data object Ok : Envio
        data class Error(val mensaje: String) : Envio
    }
    private val _envios = Channel<Envio>(Channel.BUFFERED)
    val envios: Flow<Envio> = _envios.receiveAsFlow()

    init {
        escucharSimposios()
    }

    private fun escucharSimposios() {
        listenerSimposios?.remove()
        listenerSimposios = firestore
            .collection("simposios")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _envios.trySend(
                        Envio.Error(
                            "No se pudieron cargar los simposios: ${error.message}"
                        )
                    )
                    return@addSnapshotListener
                }
                val lista = snapshot
                    ?.documents
                    ?.mapNotNull { documento ->
                        documento.toObject(
                            SimposioFirebase::class.java
                        )
                    }
                    .orEmpty()
                _simposios.value = lista
            }
    }
    fun enviar(
        simposioId: String,
        titulo: String,
        resumen: String,
        archivoUri: Uri?,
        nombreArchivo: String?
    ) {
        val uid = auth.currentUser?.uid
        val error = validar(
            uid = uid,
            simposioId = simposioId,
            titulo = titulo,
            resumen = resumen,
            archivoUri = archivoUri
        )

        if (error != null) {
            _envios.trySend(
                Envio.Error(error)
            )
            return
        }
        val uidSeguro = uid!!
        val uriSeguro = archivoUri!!
        val simposio = _simposios.value
            .firstOrNull {
                it.id == simposioId
            }
        if (simposio == null) {
            _envios.trySend(
                Envio.Error(
                    "No se encontró el simposio seleccionado"
                )
            )
            return
        }
        val trabajoRef = firestore
            .collection("trabajos")
            .document()
        val trabajoId = trabajoRef.id
        val nombreSeguro =
            (nombreArchivo ?: "trabajo.pdf")
                .replace("/", "_")
        val archivoRef = storage
            .reference
            .child(
                "trabajos/$uidSeguro/$trabajoId/$nombreSeguro"
            )

        // 1. subir el PDF a Firebase Storage
        archivoRef
            .putFile(uriSeguro)
            .addOnSuccessListener {
                // 2. obtener la URL del archivo
                archivoRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        guardarTrabajoEnFirestore(
                            trabajoRef = trabajoRef,
                            trabajoId = trabajoId,
                            uid = uidSeguro,
                            simposio = simposio,
                            titulo = titulo,
                            resumen = resumen,
                            nombreArchivo = nombreSeguro,
                            archivoUrl = downloadUri.toString(),
                            archivoRef = archivoRef
                        )
                    }
                    .addOnFailureListener { error ->
                        // el archivo llegó a Storage pero no pudo completar la operacin
                        archivoRef.delete()
                        _envios.trySend(
                            Envio.Error(
                                "El PDF se subió, pero no se pudo obtener su URL: ${error.message}"
                            )
                        )
                    }
            }
            .addOnFailureListener { error ->
                _envios.trySend(
                    Envio.Error(
                        "No se pudo subir el PDF: ${error.message}"
                    )
                )
            }
    }
    private fun guardarTrabajoEnFirestore(
        trabajoRef: com.google.firebase.firestore.DocumentReference,
        trabajoId: String,
        uid: String,
        simposio: SimposioFirebase,
        titulo: String,
        resumen: String,
        nombreArchivo: String,
        archivoUrl: String,
        archivoRef: com.google.firebase.storage.StorageReference
    ) {
        val context = getApplication<Application>()
        val trabajo = TrabajoFirebase(
            id = trabajoId,
            simposioId = simposio.id,
            simposioTitulo = simposio.titulo,
            autorUid = uid,
            autorNombre = Sesion.nombre(context),
            autorEmail = Sesion.email(context) ?: "",
            titulo = titulo.trim(),
            resumen = resumen.trim(),
            archivoUrl = archivoUrl,
            nombreArchivo = nombreArchivo,
            fechaEnvio = Timestamp.now(),
            estado = "ENVIADO",
            motivoRechazo = null,
            fechaResolucion = null,
            resueltoPorUid = null
        )
        // 3. guarda los metadatos del trabajo en Firestore
        trabajoRef
            .set(trabajo)
            .addOnSuccessListener {
                _envios.trySend(
                    Envio.Ok
                )
            }
            .addOnFailureListener { error ->
                //si firestore falla, eliminamos el PDF para no dejar un archivo huérfano
                archivoRef.delete()
                _envios.trySend(
                    Envio.Error(
                        "El PDF se subió, pero no se pudo guardar el trabajo: ${error.message}"
                    )
                )
            }
    }

    private fun validar(
        uid: String?,
        simposioId: String,
        titulo: String,
        resumen: String,
        archivoUri: Uri?
    ): String? = when {
        uid == null -> "Iniciá sesión para enviar un trabajo"
        simposioId.isBlank() -> "No se pudo identificar el simposio"
        titulo.isBlank() -> "Falta el título del trabajo"
        resumen.isBlank() -> "Falta el resumen"
        archivoUri == null -> "Falta adjuntar el PDF"
        else -> null
    }
    override fun onCleared() {
        super.onCleared()
        listenerSimposios?.remove()
    }
}