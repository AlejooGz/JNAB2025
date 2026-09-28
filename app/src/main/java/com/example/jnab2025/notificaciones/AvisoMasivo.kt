package com.example.jnab2025.notificaciones

import android.util.Log
import com.example.jnab2025.data.model.NotificacionFirebase
import com.example.jnab2025.data.model.Rol
import com.example.jnab2025.data.model.TipoNotificacion
import com.example.jnab2025.data.model.UsuarioFirebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

/**
 * Avisos que el organizador manda a todos los expositores y asistentes
 * (lugar nuevo en el mapa, novedad publicada).
 *
 * Es fan-out: un doc de `notificaciones` por usuario, igual que el aviso de
 * pago, para que el lado que entrega (listener en vivo, worker, campanita,
 * "leida") no cambie. Hay que llamarlo recien cuando lo que se avisa ya quedo
 * guardado, asi un fallo al avisar no impide guardarlo.
 *
 * Los usuarios que se registren despues no reciben el aviso.
 */
object AvisoMasivo {

    private const val TAG = "AvisoMasivo"

    /** Un batch admite hasta 500 escrituras. */
    private const val MAX_ESCRITURAS_BATCH = 500

    /**
     * @param onFallo se llama si no se pudieron leer los usuarios o si falla
     * alguno de los batch, para que la pantalla avise si le interesa.
     */
    fun aExpositoresYAsistentes(
        tipo: TipoNotificacion,
        titulo: String,
        mensaje: String,
        referenciaId: String,
        onFallo: (Exception) -> Unit = {}
    ) {
        val firestore = FirebaseFirestore.getInstance()

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

                if (destinatarios.isEmpty()) return@addOnSuccessListener

                val ahora = Timestamp.now()

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
                                    tipo = tipo.name,
                                    titulo = titulo,
                                    mensaje = mensaje,
                                    referenciaId = referenciaId,
                                    creadaEn = ahora
                                )
                            )
                        }
                        batch
                            .commit()
                            .addOnFailureListener { exception ->
                                Log.e(TAG, "No se pudo avisar ${tipo.name} de $referenciaId", exception)
                                onFallo(exception)
                            }
                    }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "No se pudieron leer los usuarios a avisar", exception)
                onFallo(exception)
            }
    }
}
