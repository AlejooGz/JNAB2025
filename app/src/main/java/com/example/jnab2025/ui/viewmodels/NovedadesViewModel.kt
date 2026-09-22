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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NovedadesViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var listenerNovedades: com.google.firebase.firestore.ListenerRegistration? = null
    private val _novedades = MutableStateFlow<List<NovedadFirebase>>(emptyList())
    val novedades: StateFlow<List<NovedadFirebase>> = _novedades.asStateFlow()
    private val _publicando = MutableStateFlow(false)
    val publicando: StateFlow<Boolean> = _publicando.asStateFlow()
    private val _eventos =
        MutableStateFlow<Evento?>(null)
    val eventos: StateFlow<Evento?> = _eventos.asStateFlow()

    init {
        escucharNovedades()
    }
    sealed class Evento {
        data object Publicada : Evento()
        data object Actualizada : Evento()
        data object Eliminada : Evento()
        data class Error(
            val mensaje: String
        ) : Evento()
    }
    private fun escucharNovedades() {

        listenerNovedades?.remove()

        listenerNovedades = firestore
            .collection("novedades")
            .whereEqualTo("publicada", true)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    _eventos.value = Evento.Error(
                        error.message ?: "Error al cargar las novedades"
                    )
                    return@addSnapshotListener
                }

                val lista = snapshot
                    ?.documents
                    ?.mapNotNull { documento ->

                        documento
                            .toObject(NovedadFirebase::class.java)
                            ?.copy(
                                id = documento.id
                            )
                    }
                    ?.sortedByDescending {
                        it.fechaPublicacion
                    }
                    .orEmpty()

                _novedades.value = lista
            }
    }
    fun obtenerNovedad(
        novedadId: String,
        onResultado: (NovedadFirebase?) -> Unit
    ) {

        if (novedadId.isBlank()) {
            onResultado(null)
            return
        }

        firestore
            .collection("novedades")
            .document(novedadId)
            .get()
            .addOnSuccessListener { documento ->

                if (documento.exists()) {

                    val novedad =
                        documento
                            .toObject(NovedadFirebase::class.java)
                            ?.copy(
                                id = documento.id
                            )

                    onResultado(novedad)

                } else {

                    onResultado(null)
                }
            }
            .addOnFailureListener {

                _eventos.value = Evento.Error(
                    it.message ?: "No se pudo cargar la novedad"
                )

                onResultado(null)
            }
    }
    fun publicar(
        titulo: String,
        descripcion: String,
        imagenUri: Uri?
    ) {

        val tituloLimpio = titulo.trim()
        val descripcionLimpia = descripcion.trim()

        if (auth.currentUser == null) {

            _eventos.value = Evento.Error(
                "No hay un usuario autenticado"
            )

            return
        }

        if (tituloLimpio.isBlank()) {

            _eventos.value = Evento.Error(
                "Ingresá un título para la novedad"
            )

            return
        }

        if (descripcionLimpia.isBlank()) {

            _eventos.value = Evento.Error(
                "Ingresá una descripción para la novedad"
            )

            return
        }

        viewModelScope.launch {

            _publicando.value = true

            try {

                val usuarioUid = auth.currentUser!!.uid

                val referenciaNovedad =
                    firestore
                        .collection("novedades")
                        .document()

                val novedadId = referenciaNovedad.id
                var imagenUrl: String? = null

                if (imagenUri != null) {

                    val referenciaImagen =
                        storage
                            .reference
                            .child(
                                "novedades/$novedadId/imagen"
                            )

                    referenciaImagen
                        .putFile(imagenUri)
                        .await()

                    imagenUrl =
                        referenciaImagen
                            .downloadUrl
                            .await()
                            .toString()
                }

                val novedad = NovedadFirebase(
                    id = novedadId,
                    titulo = tituloLimpio,
                    descripcion = descripcionLimpia,
                    imagenUrl = imagenUrl,
                    fechaPublicacion = Timestamp.now(),
                    autorUid = usuarioUid,
                    publicada = true
                )

                referenciaNovedad
                    .set(novedad)
                    .await()

                _eventos.value = Evento.Publicada

            } catch (e: Exception) {

                _eventos.value = Evento.Error(
                    e.message ?: "No se pudo publicar la novedad"
                )

            } finally {

                _publicando.value = false
            }
        }
    }

    fun actualizar(
        novedadId: String,
        titulo: String,
        descripcion: String,
        imagenUri: Uri?,
        imagenActualUrl: String?
    ) {

        val tituloLimpio = titulo.trim()
        val descripcionLimpia = descripcion.trim()

        if (auth.currentUser == null) {
            _eventos.value = Evento.Error(
                "No hay un usuario autenticado"
            )
            return
        }

        if (novedadId.isBlank()) {
            _eventos.value = Evento.Error(
                "No se encontró la novedad a editar"
            )
            return
        }

        if (tituloLimpio.isBlank()) {
            _eventos.value = Evento.Error(
                "Ingresá un título para la novedad"
            )
            return
        }

        if (descripcionLimpia.isBlank()) {
            _eventos.value = Evento.Error(
                "Ingresá una descripción para la novedad"
            )
            return
        }

        viewModelScope.launch {
            _publicando.value = true
            try {
                val referenciaNovedad =
                    firestore
                        .collection("novedades")
                        .document(novedadId)

                var imagenUrlFinal = imagenActualUrl

                if (imagenUri != null) {
                    val referenciaImagen =
                        storage
                            .reference
                            .child(
                                "novedades/$novedadId/imagen"
                            )

                    // se reutiliza la misma ubicación y la imagen anterior se reemplaza
                    referenciaImagen
                        .putFile(imagenUri)
                        .await()

                    imagenUrlFinal =
                        referenciaImagen
                            .downloadUrl
                            .await()
                            .toString()
                }
                val cambios = hashMapOf<String, Any?>(
                    "titulo" to tituloLimpio,
                    "descripcion" to descripcionLimpia,
                    "imagenUrl" to imagenUrlFinal
                )

                referenciaNovedad
                    .update(cambios)
                    .await()

                _eventos.value = Evento.Actualizada

            } catch (e: Exception) {
                _eventos.value = Evento.Error(
                    e.message ?: "No se pudo actualizar la novedad"
                )

            } finally {
                _publicando.value = false
            }
        }
    }
    fun eliminar(novedad: NovedadFirebase) {

        if (auth.currentUser == null) {
            _eventos.value = Evento.Error(
                "No hay un usuario autenticado"
            )

            return
        }

        if (novedad.id.isBlank()) {
            _eventos.value = Evento.Error(
                "No se encontró el identificador de la novedad"
            )

            return
        }

        viewModelScope.launch {
            _publicando.value = true

            try {
                if (!novedad.imagenUrl.isNullOrBlank()) {
                    val referenciaImagen =
                        storage
                            .reference
                            .child(
                                "novedades/${novedad.id}/imagen"
                            )

                    referenciaImagen
                        .delete()
                        .await()
                }
                firestore
                    .collection("novedades")
                    .document(novedad.id)
                    .delete()
                    .await()

                _eventos.value = Evento.Eliminada

            } catch (e: Exception) {
                _eventos.value = Evento.Error(
                    e.message ?: "No se pudo eliminar la novedad"
                )

            } finally {
                _publicando.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerNovedades?.remove()
        listenerNovedades = null
    }
}