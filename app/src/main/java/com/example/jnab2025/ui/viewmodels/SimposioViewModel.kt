package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.SimposioFirebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SimposioViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val _simposios = MutableStateFlow<List<SimposioFirebase>>(emptyList())
    val simposios: StateFlow<List<SimposioFirebase>> = _simposios.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private var listenerSimposios: ListenerRegistration? = null
    init {
        escucharSimposios()
    }
    private fun escucharSimposios() {
        listenerSimposios?.remove()
        listenerSimposios = firestore
            .collection("simposios")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _error.value = "No se pudieron cargar los simposios: ${error.message}"
                    _simposios.value = emptyList()
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
                _simposios.value = lista
                _error.value = null
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerSimposios?.remove()
    }
}