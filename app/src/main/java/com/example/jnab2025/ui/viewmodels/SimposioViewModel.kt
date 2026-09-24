package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.SimposioFirebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Listado general de simposios, el que ve cualquier usuario.
 *
 * Solo se publican los simposios que ya tienen al menos una charla programada.
 * Un simposio recien creado, o uno al que todavia no le aprobaron ningun
 * trabajo, no tiene nada que mostrar: aparecia en la lista y al abrirlo estaba
 * vacio. Como la charla se crea recien cuando el organizador programa un
 * trabajo aprobado, tener una charla equivale a tener contenido confirmado.
 *
 * Ojo: este filtro es solo para esta pantalla. "Mis Simposios" del organizador
 * y el selector de simposio al enviar un trabajo siguen mostrandolos todos, si
 * no seria imposible cargarle el primer trabajo a un simposio nuevo.
 */
class SimposioViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()

    private val _todos = MutableStateFlow<List<SimposioFirebase>>(emptyList())
    private val _charlas = MutableStateFlow<List<CharlaFirebase>>(emptyList())

    /** Simposios con al menos una charla programada, ordenados por fecha. */
    val simposios: StateFlow<List<SimposioFirebase>> =
        combine(_todos, _charlas) { simposios, charlas ->
            val conCharla = charlas
                .mapNotNull { charla -> charla.simposioId }
                .filter { id -> id.isNotBlank() }
                .toSet()

            simposios.filter { simposio -> simposio.id in conCharla }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    /** Hay simposios cargados, pero ninguno llego a tener charlas. */
    val hayOcultos: StateFlow<Boolean> =
        combine(_todos, simposios) { todos, visibles ->
            todos.isNotEmpty() && visibles.isEmpty()
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            false
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var listenerSimposios: ListenerRegistration? = null
    private var listenerCharlas: ListenerRegistration? = null

    init {
        escucharSimposios()
        escucharCharlas()
    }

    private fun escucharSimposios() {
        listenerSimposios?.remove()
        listenerSimposios = firestore
            .collection("simposios")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _error.value = "No se pudieron cargar los simposios: ${error.message}"
                    _todos.value = emptyList()
                    return@addSnapshotListener
                }
                val lista = snapshot
                    ?.documents
                    ?.mapNotNull { documento -> documento.toObject(
                            SimposioFirebase::class.java
                        )
                    }
                    ?.sortedBy { simposio -> simposio.fechaInicio
                    }
                    .orEmpty()
                _todos.value = lista
                _error.value = null
            }
    }

    /**
     * Se escucha el cronograma entero, no las charlas de un simposio, porque
     * la lista de simposios cambia sola: en cuanto el organizador programa la
     * primera presentacion de un simposio, ese simposio aparece.
     */
    private fun escucharCharlas() {
        listenerCharlas?.remove()
        listenerCharlas = firestore
            .collection("charlas")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _error.value = "No se pudieron cargar las charlas: ${error.message}"
                    _charlas.value = emptyList()
                    return@addSnapshotListener
                }
                _charlas.value = snapshot
                    ?.documents
                    ?.mapNotNull { documento -> documento.toObject(
                            CharlaFirebase::class.java
                        )
                    }
                    .orEmpty()
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerSimposios?.remove()
        listenerCharlas?.remove()
    }
}
