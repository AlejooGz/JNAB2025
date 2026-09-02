package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.data.model.TrabajoFirebase
import com.example.jnab2025.utils.Sesion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeOrganizadorViewModel(
    application: Application
) : AndroidViewModel(application) {

    data class Resumen(
        val cantidadSimposios: Int = 0,
        val propuestasPendientes: Int = 0,
        val pagosPendientes: Int = 0,
        val cargando: Boolean = true
    )

    private val firestore = FirebaseFirestore.getInstance()
    private val _resumen = MutableStateFlow(Resumen())

    val resumen: StateFlow<Resumen> = _resumen.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)

    val error: StateFlow<String?> = _error.asStateFlow()
    private var listenerSimposios: ListenerRegistration? = null
    private var listenerTrabajos: ListenerRegistration? = null
    private var listenerComprobantes: ListenerRegistration? = null
    private var idsSimposios: Set<String> = emptySet()
    private var trabajosActuales: List<TrabajoFirebase> = emptyList()

    init {
        escucharSimposios()
        escucharTrabajos()
        escucharComprobantes()
    }
    private fun escucharSimposios() {
        val organizadorUid =
            Sesion.firebaseUid(getApplication())

        if (organizadorUid == null) {
            _error.value =
                "No hay un organizador autenticado"

            _resumen.value =
                _resumen.value.copy(
                    cargando = false
                )
            return
        }
        listenerSimposios?.remove()

        listenerSimposios = firestore
            .collection("simposios")
            .whereEqualTo(
                "organizadorUid",
                organizadorUid
            )
            .addSnapshotListener {
                    snapshot,
                    error ->

                if (error != null) {
                    _error.value =
                        "No se pudieron cargar los simposios: ${error.message}"

                    _resumen.value =
                        _resumen.value.copy(
                            cargando = false
                        )

                    return@addSnapshotListener
                }

                val simposios = snapshot
                    ?.documents
                    ?.mapNotNull { documento ->
                        documento.toObject(
                            SimposioFirebase::class.java
                        )
                    }
                    .orEmpty()

                idsSimposios =
                    simposios
                        .map { it.id }
                        .toSet()

                _resumen.value =
                    _resumen.value.copy(
                        cantidadSimposios =
                            simposios.size,
                        cargando = false
                    )

                actualizarCantidadPropuestas()
                _error.value = null
            }
    }
    private fun escucharTrabajos() {
        listenerTrabajos?.remove()
        listenerTrabajos = firestore
            .collection("trabajos")
            .whereEqualTo(
                "estado",
                "ENVIADO"
            )
            .addSnapshotListener {
                    snapshot,
                    error ->

                if (error != null) {
                    _error.value =
                        "No se pudieron cargar las propuestas: ${error.message}"

                    return@addSnapshotListener
                }
                trabajosActuales = snapshot
                    ?.documents
                    ?.mapNotNull { documento ->
                        documento.toObject(
                            TrabajoFirebase::class.java
                        )
                    }
                    .orEmpty()

                actualizarCantidadPropuestas()
            }
    }
    private fun actualizarCantidadPropuestas() {
        val cantidad = trabajosActuales.count {
            it.simposioId in idsSimposios
        }
        _resumen.value =
            _resumen.value.copy(
                propuestasPendientes = cantidad
            )
    }
    private fun escucharComprobantes() {
        listenerComprobantes?.remove()
        listenerComprobantes = firestore
            .collection("comprobantes")
            .whereEqualTo(
                "estado",
                "PENDIENTE"
            )
            .addSnapshotListener {
                    snapshot,
                    error ->

                if (error != null) {
                    _error.value =
                        "No se pudieron cargar los pagos pendientes: ${error.message}"

                    return@addSnapshotListener
                }
                _resumen.value =
                    _resumen.value.copy(
                        pagosPendientes =
                            snapshot?.size() ?: 0
                    )
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerSimposios?.remove()
        listenerTrabajos?.remove()
        listenerComprobantes?.remove()
    }
}