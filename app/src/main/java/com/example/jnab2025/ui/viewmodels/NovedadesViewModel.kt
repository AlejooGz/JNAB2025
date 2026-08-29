package com.example.jnab2025.ui.viewmodels
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.NovedadFirebase
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

class NovedadesViewModel( application: Application
) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerNovedades: ListenerRegistration? = null
    private val _novedades = MutableStateFlow<List<NovedadFirebase>>(
            emptyList()
    )
    val novedades: StateFlow<List<NovedadFirebase>> = _novedades.asStateFlow()
    private val _publicando = MutableStateFlow(false)
    val publicando: StateFlow<Boolean> = _publicando.asStateFlow()

    sealed interface Evento {
        data object Publicada : Evento

        data class Error( val mensaje: String
        ) : Evento
    }
    private val _eventos = Channel<Evento>(Channel.BUFFERED)
    val eventos: Flow<Evento> = _eventos.receiveAsFlow()

    init {
        escucharNovedades()
    }
    private fun escucharNovedades() {
        listenerNovedades?.remove()
        listenerNovedades =
            firestore
                .collection("novedades")
                .whereEqualTo(
                    "publicada",
                    true
                )
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _eventos.trySend(
                            Evento.Error(
                                "No se pudieron cargar las novedades: ${error.message}"
                            )
                        )
                        return@addSnapshotListener
                    }
                    val lista = snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento
                                    .toObject(
                                        NovedadFirebase::class.java
                                    )
                                    ?.copy(
                                        id = documento.id
                                    )
                            }
                            ?.sortedByDescending {
                                it.fechaPublicacion
                            }
                            .orEmpty()
                    _novedades.value =
                        lista
                }
    }
    fun publicar(
        titulo: String,
        descripcion: String,
        imagenUri: Uri?
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            _eventos.trySend(
                Evento.Error(
                    "Tenés que iniciar sesión para publicar"
                )
            )
            return
        }
        val tituloLimpio = titulo.trim()
        val descripcionLimpia = descripcion.trim()
        if (tituloLimpio.isBlank()) {
            _eventos.trySend(
                Evento.Error(
                    "Ingresá un título"
                )
            )
            return
        }
        if (descripcionLimpia.isBlank()) {
            _eventos.trySend(
                Evento.Error(
                    "Ingresá una descripción"
                )
            )
            return
        }
        _publicando.value = true
        val novedadRef =
            firestore
                .collection("novedades")
                .document()
        val novedadId = novedadRef.id
        if (imagenUri == null) {
            guardarEnFirestore(
                novedadId = novedadId,
                titulo = tituloLimpio,
                descripcion = descripcionLimpia,
                imagenUrl = null
            )
            return
        }
        val imagenRef =
            storage
                .reference
                .child(
                    "novedades/$novedadId/imagen"
                )
        imagenRef
            .putFile(imagenUri)
            .addOnSuccessListener {
                imagenRef
                    .downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        guardarEnFirestore(
                            novedadId = novedadId,
                            titulo = tituloLimpio,
                            descripcion = descripcionLimpia,
                            imagenUrl =
                                downloadUri.toString()
                        )
                    }
                    .addOnFailureListener { error ->
                        imagenRef.delete()
                        terminarConError(
                            "La imagen se subió, pero no se pudo obtener su URL: ${error.message}"
                        )
                    }
            }
            .addOnFailureListener { error ->
                terminarConError(
                    "No se pudo subir la imagen: ${error.message}"
                )
            }
    }
    private fun guardarEnFirestore(
        novedadId: String,
        titulo: String,
        descripcion: String,
        imagenUrl: String?
    ) {
        val uid = auth.currentUser?.uid
                ?: run {
                    terminarConError(
                        "La sesión ya no está disponible"
                    )
                    return
                }
        val novedad =
            NovedadFirebase(
                id = novedadId,
                titulo = titulo,
                descripcion = descripcion,
                imagenUrl = imagenUrl,
                fechaPublicacion =
                    Timestamp.now(),
                autorUid = uid,
                publicada = true
            )
        firestore
            .collection("novedades")
            .document(novedadId)
            .set(novedad)
            .addOnSuccessListener {
                _publicando.value =
                    false
                _eventos.trySend(
                    Evento.Publicada
                )
            }
            .addOnFailureListener { error ->
                //si había imagen, evitamos dejarla huérfana en Storage
                if (imagenUrl != null) {
                    storage
                        .reference
                        .child(
                            "novedades/$novedadId/imagen"
                        )
                        .delete()
                }
                terminarConError(
                    "No se pudo publicar la novedad: ${error.message}"
                )
            }
    }
    private fun terminarConError(mensaje: String
    ) {
        _publicando.value = false
        _eventos.trySend(
            Evento.Error(mensaje)
        )
    }
    override fun onCleared() {
        super.onCleared()
        listenerNovedades?.remove()
    }
}