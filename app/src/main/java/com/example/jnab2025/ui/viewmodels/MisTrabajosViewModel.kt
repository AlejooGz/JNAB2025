package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.data.model.TrabajoFirebase
import com.example.jnab2025.data.model.TrabajoSeguimientoFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import java.time.ZoneId

class MisTrabajosViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var listenerTrabajos: ListenerRegistration? = null
    private var listenerCharlas: ListenerRegistration? = null
    private var listenerSimposios: ListenerRegistration? = null
    private var trabajosFirebase: List<TrabajoFirebase> = emptyList()
    private var charlasFirebase: List<CharlaFirebase> = emptyList()
    private var simposiosFirebase: List<SimposioFirebase> = emptyList()

    val haySesion: Boolean
        get() = auth.currentUser != null
    private val _trabajos = MutableStateFlow<List<TrabajoSeguimientoFirebase>>(emptyList())

    val trabajos: StateFlow<List<TrabajoSeguimientoFirebase>> =
        _trabajos.asStateFlow()
    private val _error =
        MutableStateFlow<String?>(null)
    val error: StateFlow<String?> =
        _error.asStateFlow()
    init {
        escucharDatos()
    }
    private fun escucharDatos() {
        val uid = auth.currentUser?.uid
        if (uid == null) { _trabajos.value = emptyList()
            return
        }
        escucharTrabajos(uid)
        escucharCharlas()
        escucharSimposios()
    }
    // trae únicamente los trabajos del expositor logueado actualmente
    private fun escucharTrabajos(
        uid: String
    ) {
        listenerTrabajos?.remove()
        listenerTrabajos = firestore
            .collection("trabajos")
            .whereEqualTo(
                "autorUid",
                uid
            )
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _error.value =
                        "No se pudieron cargar tus trabajos: ${error.message}"
                    return@addSnapshotListener
                }
                trabajosFirebase = snapshot
                    ?.documents
                    ?.mapNotNull { documento -> documento.toObject(
                            TrabajoFirebase::class.java
                        )
                    }
                    .orEmpty()
                reconstruirLista()
            }
    }
    private fun escucharCharlas() {
        listenerCharlas?.remove()
        listenerCharlas = firestore
            .collection("charlas")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _error.value = "No se pudo cargar la programación: ${error.message}"
                    return@addSnapshotListener
                }
                charlasFirebase = snapshot
                    ?.documents
                    ?.mapNotNull { documento ->
                        documento.toObject(
                            CharlaFirebase::class.java
                        )
                    }
                    .orEmpty()
                reconstruirLista()
            }
    }
    private fun escucharSimposios() {
        listenerSimposios?.remove()
        listenerSimposios = firestore
            .collection("simposios")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _error.value =
                        "No se pudieron cargar los simposios: ${error.message}"
                    return@addSnapshotListener
                }
                simposiosFirebase = snapshot
                    ?.documents
                    ?.mapNotNull { documento -> documento.toObject(
                            SimposioFirebase::class.java
                        )
                    }
                    .orEmpty()
                reconstruirLista()
            }
    }
    private fun reconstruirLista() {
        val lista = trabajosFirebase.map { trabajo ->
            val charla = charlasFirebase
                .firstOrNull {
                    it.trabajoId == trabajo.id
                }
            val simposio = simposiosFirebase
                .firstOrNull {
                    it.id == trabajo.simposioId
                }
            val fecha = charla
                ?.fecha
                ?.toDate()
                ?.toInstant()
                ?.atZone(
                    ZoneId.systemDefault()
                )
                ?.toLocalDate()
            val horaInicio = charla
                ?.horaInicio
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { texto ->
                    runCatching {
                        LocalTime.parse(texto)
                    }.getOrNull()
                }
            val horaFin = charla
                ?.horaFin
                ?.takeIf {
                    it.isNotBlank()
                }
                ?.let { texto ->
                    runCatching {
                        LocalTime.parse(texto)
                    }.getOrNull()
                }
            TrabajoSeguimientoFirebase(
                trabajo = trabajo,
                fecha = fecha,
                horaInicio = horaInicio,
                horaFin = horaFin,
                aula = simposio?.aulaNombre
            )
        }
        _trabajos.value = lista.sortedByDescending { seguimiento ->
            seguimiento
                .trabajo
                .fechaEnvio
                ?.seconds
                ?: 0L
        }
        _error.value = null
    }

    override fun onCleared() {
        super.onCleared()
        listenerTrabajos?.remove()
        listenerCharlas?.remove()
        listenerSimposios?.remove()
    }
}