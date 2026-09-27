package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.AulaFirebase
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.data.model.TrabajoFirebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Junta todo lo que se sabe de una charla para la pantalla de detalle.
 *
 * Una charla sola no alcanza: guarda ids, no textos. El expositor y el resumen
 * estan en el trabajo, el tema y la descripcion en el simposio, y el nombre del
 * aula en la coleccion aulas. Antes la pantalla leia solo la charla y por eso
 * mostraba el id del documento como si fuera el nombre del aula, y rellenaba el
 * expositor y la descripcion con texto fijo.
 */
class CharlaDetalleViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val firestore = FirebaseFirestore.getInstance()

    data class Detalle(
        val charla: CharlaFirebase,
        val trabajo: TrabajoFirebase? = null,
        val simposio: SimposioFirebase? = null,
        val aula: AulaFirebase? = null
    )

    private val _detalle = MutableStateFlow<Detalle?>(null)
    val detalle: StateFlow<Detalle?> = _detalle.asStateFlow()

    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()

    private var listenerCharla: ListenerRegistration? = null

    /* Que ids ya se resolvieron, para no volver a pedir lo mismo cada vez que
     * el listener de la charla emite. */
    private var trabajoCargado: String? = null
    private var simposioCargado: String? = null
    private var aulaCargada: String? = null

    fun cargar(charlaId: String) {
        listenerCharla?.remove()

        listenerCharla = firestore
            .collection("charlas")
            .document(charlaId)
            .addSnapshotListener { documento, error ->

                if (error != null) {
                    _avisos.trySend(
                        "No se pudo cargar la charla: ${error.message}"
                    )
                    return@addSnapshotListener
                }

                val charla = documento?.toObject(CharlaFirebase::class.java)
                if (charla == null) {
                    _avisos.trySend("No se encontró la charla")
                    return@addSnapshotListener
                }

                _detalle.value = _detalle.value
                    ?.copy(charla = charla)
                    ?: Detalle(charla = charla)

                cargarTrabajo(charla.trabajoId)
                cargarSimposio(charla.simposioId)
                cargarAula(charla.aulaId)
            }
    }

    private fun cargarTrabajo(trabajoId: String?) {
        if (trabajoId.isNullOrBlank() || trabajoId == trabajoCargado) return
        trabajoCargado = trabajoId

        firestore
            .collection("trabajos")
            .document(trabajoId)
            .get()
            .addOnSuccessListener { documento ->
                _detalle.value = _detalle.value?.copy(
                    trabajo = documento.toObject(TrabajoFirebase::class.java)
                )
            }
            .addOnFailureListener {
                // El detalle igual se muestra: sin el trabajo pierde el resumen
                // y el expositor, pero la fecha y el lugar siguen sirviendo.
                trabajoCargado = null
            }
    }

    private fun cargarSimposio(simposioId: String?) {
        if (simposioId.isNullOrBlank() || simposioId == simposioCargado) return
        simposioCargado = simposioId

        firestore
            .collection("simposios")
            .document(simposioId)
            .get()
            .addOnSuccessListener { documento ->
                _detalle.value = _detalle.value?.copy(
                    simposio = documento.toObject(SimposioFirebase::class.java)
                )
            }
            .addOnFailureListener { simposioCargado = null }
    }

    private fun cargarAula(aulaId: String) {
        if (aulaId.isBlank() || aulaId == aulaCargada) return
        aulaCargada = aulaId

        firestore
            .collection("aulas")
            .document(aulaId)
            .get()
            .addOnSuccessListener { documento ->
                _detalle.value = _detalle.value?.copy(
                    aula = documento.toObject(AulaFirebase::class.java)
                )
            }
            .addOnFailureListener { aulaCargada = null }
    }

    override fun onCleared() {
        super.onCleared()
        listenerCharla?.remove()
    }
}
