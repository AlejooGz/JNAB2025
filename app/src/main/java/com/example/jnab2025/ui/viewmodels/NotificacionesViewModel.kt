package com.example.jnab2025.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.NotificacionFirebase
import com.example.jnab2025.notificaciones.SincronizadorNotificaciones
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Escucha las notificaciones del usuario logueado mientras la app esta abierta
 * y las emite al instante.
 *
 * Vive atado a MainActivity, que hospeda todas las pantallas, para que el aviso
 * llegue este donde este el usuario. El worker cubre el caso de la app cerrada;
 * ambos comparten el filtro de "ya mostrada" para no duplicar.
 */
class NotificacionesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _noLeidas = MutableStateFlow(0)
    val noLeidas: StateFlow<Int> = _noLeidas.asStateFlow()

    private var listenerNotificaciones: ListenerRegistration? = null
    private var uidEscuchado: String? = null

    init {
        escuchar()
    }

    /**
     * Se vuelve a llamar desde la Activity al cambiar de sesion: el UID puede
     * haber cambiado desde que se creo el ViewModel.
     */
    fun escuchar() {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            listenerNotificaciones?.remove()
            listenerNotificaciones = null
            uidEscuchado = null
            _noLeidas.value = 0
            return
        }

        // Ya estamos escuchando a este usuario: no rearmamos el listener.
        if (uid == uidEscuchado && listenerNotificaciones != null) return

        listenerNotificaciones?.remove()
        uidEscuchado = uid

        listenerNotificaciones =
            firestore
                .collection("notificaciones")
                .whereEqualTo("destinatarioUid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener

                    val notificaciones =
                        snapshot.documents
                            .mapNotNull {
                                it.toObject(NotificacionFirebase::class.java)
                            }

                    notificaciones.forEach { notificacion ->
                        SincronizadorNotificaciones.emitirSiEsNueva(
                            getApplication(),
                            notificacion
                        )
                    }

                    _noLeidas.value = notificaciones.count { !it.leida }
                }
    }

    override fun onCleared() {
        super.onCleared()
        listenerNotificaciones?.remove()
    }
}
