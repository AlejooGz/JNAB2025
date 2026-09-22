package com.example.jnab2025.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.jnab2025.data.firebase.LugaresIniciales
import com.example.jnab2025.data.model.LugarFirebase
import com.example.jnab2025.data.model.NotificacionFirebase
import com.example.jnab2025.data.model.Rol
import com.example.jnab2025.data.model.TipoNotificacion
import com.example.jnab2025.data.model.UsuarioFirebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import java.text.Normalizer
import java.util.Locale

class LugaresViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null
    private val _todosLugares = MutableStateFlow<List<LugarFirebase>>(emptyList())
    val todosLugares: StateFlow<List<LugarFirebase>> = _todosLugares.asStateFlow()
    private val _lugares =
        MutableStateFlow<List<LugarFirebase>>(emptyList())
    val lugares: StateFlow<List<LugarFirebase>> = _lugares.asStateFlow()
    private val _avisos = Channel<String>(Channel.BUFFERED)
    val avisos = _avisos.receiveAsFlow()
    // true mientras se guarda el formulario de crear/editar lugar
    private val _enviando = MutableStateFlow(false)
    val enviando: StateFlow<Boolean> = _enviando.asStateFlow()

    init {
        escucharLugares()
    }
    private fun escucharLugares() {
        listener?.remove()
        listener =
            firestore
                .collection("lugares")
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        _avisos.trySend(
                            exception.message
                                ?: "No se pudieron cargar los lugares"
                        )
                        return@addSnapshotListener
                    }
                    val lista =
                        snapshot
                            ?.documents
                            ?.mapNotNull { documento ->
                                documento
                                    .toObject(
                                        LugarFirebase::class.java
                                    )
                                    ?.copy(
                                        id = documento.id
                                    )
                            }
                            ?.sortedBy {
                                it.nombre.lowercase()
                            }
                            .orEmpty()
                    _todosLugares.value = lista
                    _lugares.value =
                        lista.filter {
                            it.activo
                        }
                }
    }

    fun crearLugar(lugar: LugarFirebase
    ) {
        val referencia =
            firestore
                .collection("lugares")
                .document()
        val nuevoLugar =
            lugar.copy(
                id = referencia.id,
                activo = true
            )
        _enviando.value = true
        referencia
            .set(nuevoLugar)
            .addOnSuccessListener {
                terminar(
                    "Lugar creado correctamente"
                )
                notificarLugar(
                    nuevoLugar,
                    reactivado = false
                )
            }
            .addOnFailureListener { exception ->
                terminar(
                    exception.message
                        ?: "No se pudo crear el lugar"
                )
            }
    }

    // cierra el guardado del formulario: apaga la ruedita y avisa el resultado
    private fun terminar(mensaje: String) {
        _enviando.value = false
        _avisos.trySend(mensaje)
    }

    fun editarLugar(lugar: LugarFirebase
    ) {
        if (lugar.id.isBlank()) {
            _avisos.trySend(
                "El lugar no tiene un identificador válido"
            )
            return
        }
        _enviando.value = true
        firestore
            .collection("lugares")
            .document(lugar.id)
            .set(
                lugar,
                SetOptions.merge()
            )
            .addOnSuccessListener {
                terminar(
                    "Lugar actualizado correctamente"
                )
            }
            .addOnFailureListener { exception ->
                terminar(
                    exception.message
                        ?: "No se pudo actualizar el lugar"
                )
            }
    }

    fun cambiarEstado(lugar: LugarFirebase
    ) {
        if (lugar.id.isBlank()) {
            return
        }
        val nuevoEstado = !lugar.activo
        firestore
            .collection("lugares")
            .document(lugar.id)
            .update(
                "activo",
                nuevoEstado
            )
            .addOnSuccessListener {
                _avisos.trySend(
                    if (nuevoEstado) {
                        "Lugar activado"
                    } else {
                        "Lugar desactivado"
                    }
                )
                // al reactivarlo vuelve a aparecer en el mapa: se avisa igual
                // que con un lugar nuevo
                if (nuevoEstado) {
                    notificarLugar(
                        lugar.copy(activo = true),
                        reactivado = true
                    )
                }
            }
            .addOnFailureListener { exception ->

                _avisos.trySend(
                    exception.message
                        ?: "No se pudo modificar el lugar"
                )
            }
    }
    /**
     * Deja una notificacion LUGAR_AGREGADO para cada expositor y asistente.
     * Es fan-out: un doc por usuario, igual que el aviso de pago, para que el
     * lado que entrega (listener en vivo, worker, campanita, "leida") no
     * cambie. Se llama recien cuando el lugar ya quedo guardado, asi un fallo
     * al avisar no impide crearlo.
     *
     * Los usuarios que se registren despues no reciben este aviso.
     */
    private fun notificarLugar(
        lugar: LugarFirebase,
        reactivado: Boolean
    ) {
        firestore
            .collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                // el rol se guarda como texto: se compara en mayusculas, igual
                // que en el login
                val destinatarios =
                    snapshot.documents
                        .mapNotNull { documento ->
                            val usuario =
                                documento.toObject(UsuarioFirebase::class.java)
                                    ?: return@mapNotNull null
                            val rol = usuario.rol.uppercase(Locale.ROOT)
                            if (rol != Rol.EXPOSITOR.name && rol != Rol.ASISTENTE.name) {
                                return@mapNotNull null
                            }
                            usuario.uid.ifBlank { documento.id }
                        }
                        .distinct()

                if (destinatarios.isEmpty()) {
                    return@addOnSuccessListener
                }

                val titulo =
                    if (reactivado) {
                        "Un lugar volvió al mapa"
                    } else {
                        "Nuevo lugar en el mapa"
                    }
                val mensaje =
                    buildString {
                        append(lugar.nombre)
                        append(
                            if (reactivado) {
                                " vuelve a estar en el mapa de descuentos"
                            } else {
                                " se sumó al mapa de descuentos"
                            }
                        )
                        if (lugar.descuento.isNotBlank()) {
                            append(" (")
                            append(lugar.descuento)
                            append(")")
                        }
                        append(". Tocá para verlo.")
                    }
                val ahora = Timestamp.now()

                // un batch admite hasta 500 escrituras
                destinatarios
                    .chunked(MAX_ESCRITURAS_BATCH)
                    .forEach { grupo ->
                        val batch = firestore.batch()
                        grupo.forEach { uid ->
                            val referencia =
                                firestore
                                    .collection("notificaciones")
                                    .document()
                            batch.set(
                                referencia,
                                NotificacionFirebase(
                                    id = referencia.id,
                                    destinatarioUid = uid,
                                    tipo = TipoNotificacion.LUGAR_AGREGADO.name,
                                    titulo = titulo,
                                    mensaje = mensaje,
                                    referenciaId = lugar.id,
                                    creadaEn = ahora
                                )
                            )
                        }
                        batch
                            .commit()
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "No se pudo avisar del lugar ${lugar.id}", exception)
                                _avisos.trySend(
                                    "El lugar se guardó, pero no se pudo avisar a los usuarios"
                                )
                            }
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "No se pudieron leer los usuarios a avisar", exception)
                _avisos.trySend(
                    "El lugar se guardó, pero no se pudo avisar a los usuarios"
                )
            }
    }

    // esta función se ejecuta UNA SOLA VEZ para migrar la lista original
    fun cargarLugaresIniciales(
        onResultado: (Boolean, String) -> Unit
    ) {
        val lugares =
            LugaresIniciales.lista
        if (lugares.isEmpty()) {
            onResultado(
                false,
                "No hay lugares para cargar"
            )
            return
        }
        val batch = firestore.batch()
        lugares.forEach { lugar ->
            val id =
                generarIdLugar(
                    lugar.nombre
                )
            val referencia =
                firestore
                    .collection("lugares")
                    .document(id)
            batch.set(
                referencia,
                lugar.copy(
                    id = id
                )
            )
        }
        batch
            .commit()
            .addOnSuccessListener {
                onResultado(
                    true,
                    "${lugares.size} lugares cargados correctamente"
                )
            }
            .addOnFailureListener { exception ->
                onResultado(
                    false,
                    exception.message
                        ?: "No se pudieron cargar los lugares"
                )
            }
    }
    private fun generarIdLugar(
        nombre: String
    ): String {
        val normalizado =
            Normalizer.normalize(
                nombre,
                Normalizer.Form.NFD
            )
                .replace(
                    "\\p{InCombiningDiacriticalMarks}+".toRegex(),
                    ""
                )

        return normalizado
            .lowercase(Locale.ROOT)
            .replace(
                "[^a-z0-9]+".toRegex(),
                "_"
            )
            .trim('_')
    }

    override fun onCleared() {
        super.onCleared()
        listener?.remove()
    }

    private companion object {
        const val TAG = "LugaresViewModel"
        const val MAX_ESCRITURAS_BATCH = 500
    }
}