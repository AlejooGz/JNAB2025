package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.TrabajoFirebase
import com.example.jnab2025.data.model.TrabajoSimposioUi
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow

class TrabajosSimposioViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val _trabajos = MutableStateFlow<List<TrabajoSimposioUi>>(emptyList())
    val trabajos: StateFlow<List<TrabajoSimposioUi>> = _trabajos.asStateFlow()
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos: Flow<String> = _avisos.receiveAsFlow()
    private var listenerTrabajos: ListenerRegistration? = null
    private var listenerCharlas: ListenerRegistration? = null
    private var trabajosFirebase: List<TrabajoFirebase> = emptyList()
    private var charlasFirebase: List<CharlaFirebase> = emptyList()

    fun cargar(
        simposioId: String
    ) {
        escucharTrabajos(simposioId)
        escucharCharlas(simposioId)
    }

    private fun escucharTrabajos(
        simposioId: String
    ) {
        listenerTrabajos?.remove()
        listenerTrabajos =
            firestore
                .collection("trabajos")
                .whereEqualTo(
                    "simposioId",
                    simposioId
                )
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
                            ?.mapNotNull { documento ->

                                documento.toObject(
                                    TrabajoFirebase::class.java
                                )
                            }
                            .orEmpty()
                    reconstruir()
                }
    }

    private fun escucharCharlas(
        simposioId: String
    ) {
        listenerCharlas?.remove()
        listenerCharlas =
            firestore
                .collection("charlas")
                .whereEqualTo(
                    "simposioId",
                    simposioId
                )
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {

                        _avisos.trySend(
                            "No se pudo cargar la programación: ${error.message}"
                        )

                        return@addSnapshotListener
                    }

                    charlasFirebase =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->

                                documento.toObject(
                                    CharlaFirebase::class.java
                                )
                            }
                            .orEmpty()

                    reconstruir()
                }
    }

    private fun reconstruir() {
        val charlasPorTrabajo =
            charlasFirebase
                .filter {
                    !it.trabajoId.isNullOrBlank()
                }
                .associateBy {
                    it.trabajoId!!
                }

        _trabajos.value =
            trabajosFirebase
                .map { trabajo ->

                    TrabajoSimposioUi(
                        trabajo = trabajo,
                        charla =
                            charlasPorTrabajo[
                                trabajo.id
                            ]
                    )
                }
                .sortedBy {
                    it.trabajo.titulo.lowercase()
                }
    }

    override fun onCleared() {
        super.onCleared()
        listenerTrabajos?.remove()
        listenerCharlas?.remove()
    }
}