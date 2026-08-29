package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.FaqFirebase
import com.example.jnab2025.data.model.PublicoFaq
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class FaqViewModel (
    application: Application
) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()
    private val _faqs = MutableStateFlow<List<FaqFirebase>>(emptyList())
    val faqs: StateFlow<List<FaqFirebase>> = _faqs.asStateFlow()
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()
    private var listener: ListenerRegistration? = null

    init {
        escucharFaqs()
    }
    private fun escucharFaqs() {
        listener?.remove()
        listener =
            firestore
                .collection("faqs")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        _avisos.trySend(
                            "No se pudieron cargar las preguntas: ${error.message}"
                        )
                        return@addSnapshotListener
                    }
                    _faqs.value =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento
                                    .toObject(
                                        FaqFirebase::class.java
                                    )
                                    ?.copy(
                                        id = documento.id
                                    )
                            }
                            ?.filter {
                                it.publicada
                            }
                            ?.sortedWith(
                                compareBy<FaqFirebase> {
                                    it.publico
                                }.thenBy {
                                    it.orden
                                }
                            )
                            .orEmpty()
                }
    }

    fun crear(
        publico: PublicoFaq,
        pregunta: String,
        respuesta: String,
        orden: Int
    ) {
        val preguntaLimpia = pregunta.trim()
        val respuestaLimpia = respuesta.trim()

        if (preguntaLimpia.isBlank()) {
            _avisos.trySend("Ingresá una pregunta"
            )
            return
        }
        if (respuestaLimpia.isBlank()) {
            _avisos.trySend("Ingresá una respuesta"
            )
            return
        }
        val ref =
            firestore
                .collection("faqs")
                .document()
        val faq =
            FaqFirebase(
                id = ref.id,
                publico = publico.name,
                pregunta = preguntaLimpia,
                respuesta = respuestaLimpia,
                orden = orden,
                publicada = true
            )
        ref.set(faq)
            .addOnSuccessListener {
                _avisos.trySend("Pregunta publicada"
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend("No se pudo publicar: ${error.message}"
                )
            }
    }
    fun editar(
        faq: FaqFirebase,
        publico: PublicoFaq,
        pregunta: String,
        respuesta: String,
        orden: Int
    ) {
        if (
            pregunta.isBlank() ||
            respuesta.isBlank()
        ) {
            _avisos.trySend("Completá la pregunta y la respuesta"
            )
            return
        }

        firestore
            .collection("faqs")
            .document(faq.id)
            .update(
                mapOf(
                    "publico" to publico.name,
                    "pregunta" to pregunta.trim(),
                    "respuesta" to respuesta.trim(),
                    "orden" to orden
                )
            )
            .addOnSuccessListener {
                _avisos.trySend("Pregunta actualizada"
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend("No se pudo actualizar: ${error.message}"
                )
            }
    }

    fun eliminar(
        faq: FaqFirebase
    ) {
        //baja lógica.
        firestore
            .collection("faqs")
            .document(faq.id)
            .update(
                "publicada",
                false
            )
            .addOnSuccessListener {
                _avisos.trySend("Pregunta eliminada"
                )
            }
            .addOnFailureListener { error ->
                _avisos.trySend("No se pudo eliminar: ${error.message}"
                )
            }
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}