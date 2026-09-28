package com.example.jnab2025.ui.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.example.jnab2025.data.model.ComprobanteFirebase
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.NotificacionFirebase
import com.example.jnab2025.notificaciones.DetectorComprobantes
import com.example.jnab2025.notificaciones.SincronizadorNotificaciones
import com.example.jnab2025.utils.Sesion
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
 *
 * Tambien alimenta la campanita (con [noLeidas]) y la pantalla de
 * notificaciones (con [notificaciones]), que lo obtiene con activityViewModels
 * para reusar este mismo listener en vez de abrir otro.
 */
class NotificacionesViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _noLeidas = MutableStateFlow(0)
    val noLeidas: StateFlow<Int> = _noLeidas.asStateFlow()

    /** Mas nuevas primero. null mientras todavia no llego el primer snapshot. */
    private val _notificaciones =
        MutableStateFlow<List<NotificacionFirebase>?>(null)
    val notificaciones: StateFlow<List<NotificacionFirebase>?> =
        _notificaciones.asStateFlow()

    private var listenerNotificaciones: ListenerRegistration? = null
    private var uidEscuchado: String? = null

    /** Solo para organizadores: comprobantes pendientes que generan avisos. */
    private var listenerComprobantes: ListenerRegistration? = null

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
            listenerComprobantes?.remove()
            listenerComprobantes = null
            uidEscuchado = null
            _noLeidas.value = 0
            _notificaciones.value = null
            return
        }

        // Ya estamos escuchando a este usuario: no rearmamos el listener.
        if (uid == uidEscuchado && listenerNotificaciones != null) return

        listenerNotificaciones?.remove()
        uidEscuchado = uid
        // Que no se vea ni un instante la lista del usuario anterior.
        _notificaciones.value = null
        _noLeidas.value = 0

        escucharComprobantes(uid)

        listenerNotificaciones =
            firestore
                .collection("notificaciones")
                .whereEqualTo("destinatarioUid", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // Firestore ya dio de baja el listener (por ejemplo, al
                        // cerrar sesion). Se descarta para que el proximo
                        // escuchar() lo rearme, aunque vuelva el mismo usuario.
                        listenerNotificaciones?.remove()
                        listenerNotificaciones = null
                        uidEscuchado = null
                        return@addSnapshotListener
                    }
                    if (snapshot == null) return@addSnapshotListener

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

                    // Se ordena aca y no con orderBy: combinado con el
                    // whereEqualTo, Firestore pediria crear un indice compuesto.
                    _notificaciones.value =
                        notificaciones.sortedByDescending { it.creadaEn }
                    _noLeidas.value = notificaciones.count { !it.leida }
                }
    }

    /**
     * Si el usuario es organizador, vigila los comprobantes PENDIENTES para
     * dejarse avisos COMPROBANTE_RECIBIDO (ver [DetectorComprobantes]). Con la
     * app cerrada hace lo mismo el worker.
     */
    private fun escucharComprobantes(uid: String) {
        listenerComprobantes?.remove()
        listenerComprobantes = null
        if (!Sesion.esOrganizador(getApplication())) return

        listenerComprobantes =
            firestore
                .collection("comprobantes")
                .whereEqualTo("estado", EstadoComprobante.PENDIENTE.name)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        // mismo criterio que el listener de notificaciones
                        listenerComprobantes?.remove()
                        listenerComprobantes = null
                        return@addSnapshotListener
                    }
                    val pendientes =
                        snapshot?.documents
                            ?.mapNotNull { documento ->
                                documento
                                    .toObject(ComprobanteFirebase::class.java)
                                    ?.copy(id = documento.id)
                            }
                            .orEmpty()
                    DetectorComprobantes.procesar(getApplication(), uid, pendientes)
                }
    }

    /**
     * La pantalla de notificaciones llama a esto al mostrarse. El listener
     * recibe el cambio enseguida (compensacion de latencia de Firestore), asi
     * que el contador de la campanita baja sin esperar al servidor.
     */
    fun marcarTodasLeidas() {
        val pendientes =
            _notificaciones.value.orEmpty()
                .filter { !it.leida && it.id.isNotBlank() }

        if (pendientes.isEmpty()) return

        val batch = firestore.batch()
        pendientes.forEach { notificacion ->
            batch.update(
                firestore.collection("notificaciones").document(notificacion.id),
                "leida",
                true
            )
        }
        batch.commit().addOnFailureListener {
            Log.e("Notificaciones", "No se pudieron marcar como leidas", it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        listenerNotificaciones?.remove()
        listenerComprobantes?.remove()
    }
}