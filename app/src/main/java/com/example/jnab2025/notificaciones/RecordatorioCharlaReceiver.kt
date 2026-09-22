package com.example.jnab2025.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.jnab2025.data.model.NotificacionFirebase
import com.example.jnab2025.data.model.TipoNotificacion
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Recibe la alarma que se programo 30 minutos antes de una charla y emite el
 * aviso. Corre aunque la app este cerrada: la alarma la guarda el sistema.
 *
 * Ademas deja el aviso en la coleccion `notificaciones`, para que aparezca en
 * la pantalla de notificaciones junto con los demas tipos.
 */
class RecordatorioCharlaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val charlaId = intent.getStringExtra(EXTRA_CHARLA_ID) ?: return
        val titulo = intent.getStringExtra(EXTRA_TITULO).orEmpty()
        val lugar = intent.getStringExtra(EXTRA_LUGAR)
        val hora = intent.getStringExtra(EXTRA_HORA).orEmpty()

        val tituloAviso = "En 30 minutos: $titulo"
        val mensaje = buildString {
            append("Empieza a las ")
            append(hora)
            if (!lugar.isNullOrBlank()) {
                append(" en ")
                append(lugar)
            }
            append(".")
        }

        Notificaciones.mostrar(
            context = context,
            id = Notificaciones.idRecordatorio(charlaId),
            canal = Notificaciones.CANAL_RECORDATORIOS,
            titulo = tituloAviso,
            mensaje = mensaje
        )

        guardarEnHistorial(charlaId, tituloAviso, mensaje)
    }

    /**
     * Escribe el doc del recordatorio. goAsync() mantiene vivo el proceso
     * mientras Firestore lo registra; sin red la escritura igual queda en la
     * cache local y se sube cuando vuelve la conexion, por eso alcanza con
     * esperar un tope corto y no hasta que el servidor confirme.
     */
    private fun guardarEnHistorial(
        charlaId: String,
        titulo: String,
        mensaje: String
    ) {
        // Las alarmas se cancelan al cerrar sesion, asi que el usuario actual
        // es el dueño de la agenda que la programo.
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Id fijo por usuario y charla: si la alarma se reprograma y vuelve a
        // sonar, se pisa el mismo doc en vez de duplicarlo.
        val id = "recordatorio_${uid}_$charlaId"

        val pendiente = goAsync()
        val terminado = AtomicBoolean(false)
        val terminar = {
            if (terminado.compareAndSet(false, true)) pendiente.finish()
        }

        FirebaseFirestore.getInstance()
            .collection("notificaciones")
            .document(id)
            .set(
                NotificacionFirebase(
                    id = id,
                    destinatarioUid = uid,
                    tipo = TipoNotificacion.RECORDATORIO_CHARLA.name,
                    titulo = titulo,
                    mensaje = mensaje,
                    referenciaId = charlaId,
                    creadaEn = Timestamp.now()
                )
            )
            .addOnFailureListener {
                Log.e(TAG, "No se pudo guardar el recordatorio $charlaId", it)
            }
            .addOnCompleteListener { terminar() }

        Handler(Looper.getMainLooper()).postDelayed(terminar, ESPERA_MAXIMA_MS)
    }

    companion object {
        const val EXTRA_CHARLA_ID = "charlaId"
        const val EXTRA_TITULO = "titulo"
        const val EXTRA_LUGAR = "lugar"
        const val EXTRA_HORA = "hora"

        private const val TAG = "RecordatorioCharla"

        /** Por debajo de los ~10 s que Android le da a un receiver. */
        private const val ESPERA_MAXIMA_MS = 8_000L
    }
}