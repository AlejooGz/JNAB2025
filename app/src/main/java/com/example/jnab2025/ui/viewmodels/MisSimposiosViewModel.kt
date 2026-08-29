package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.utils.Sesion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MisSimposiosViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val firestore = FirebaseFirestore.getInstance()
    private val _simposios =
        MutableStateFlow<List<SimposioFirebase>>(emptyList())
    val simposios: StateFlow<List<SimposioFirebase>> =
        _simposios.asStateFlow()
    private var listener: ListenerRegistration? = null

    init {
        escucharMisSimposios()
    }

    private fun escucharMisSimposios() {

        val uid = Sesion.firebaseUid(getApplication())

        if (uid == null) {
            _simposios.value = emptyList()
            return
        }

        listener = firestore
            .collection("simposios")
            .whereEqualTo("organizadorUid", uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    _simposios.value = emptyList()
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

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }
}