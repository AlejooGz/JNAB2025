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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Recibe la alarma del recordatorio de una charla de Mi agenda y emite el
 * aviso. Corre aunque la app este cerrada: la alarma la guarda el sistema.
 *
 * La alarma suena 30 minutos antes, o apenas se programa si ya estamos dentro
 * de esa media hora (por ejemplo, al iniciar sesion a las 16:45 para una
 * charla de las 17). Por eso aca se decide si de verdad hay que avisar: solo
 * si la charla no empezo y el aviso no salio antes, ni en este dispositivo
 * (registro local) ni en otro (doc de historial en Firestore).
 *
 * Ademas deja el aviso en la coleccion `notificaciones`, para que aparezca en
 * la pantalla de notificaciones junto con los demas tipos.
 */
class RecordatorioCharlaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val charlaId = intent.getStringExtra(EXTRA_CHARLA_ID) ?: return
        // Las alarmas se cancelan al cerrar sesion, asi que el usuario actual
        // es el dueño de la agenda que la programo.
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        val inicioMillis = intent.getLongExtra(EXTRA_INICIO_MILLIS, 0L)
        val ahora = System.currentTimeMillis()

        // Ya empezo (el sistema demoro la alarma): el aviso no sirve.
        if (inicioMillis > 0 && ahora >= inicioMillis) return

        val idAviso =
            if (inicioMillis > 0) {
                idHistorial(uid, charlaId, aLocalDateTime(inicioMillis))
            } else {
                // alarma programada por una version anterior, sin el inicio
                "recordatorio_${uid}_$charlaId"
            }

        if (RegistroNotificaciones.yaMostrada(context, idAviso)) return

        val app = context.applicationContext
        val pendiente = goAsync()
        val terminado = AtomicBoolean(false)
        val terminar = {
            if (terminado.compareAndSet(false, true)) pendiente.finish()
        }

        // Una sola decision por alarma, llegue primero Firestore o el tope.
        val decidido = AtomicBoolean(false)
        val decidir = { yaSalio: Boolean ->
            if (decidido.compareAndSet(false, true)) {
                if (yaSalio) {
                    // lo mostro otro dispositivo: se anota para no volver a
                    // consultar Firestore en la proxima pasada
                    RegistroNotificaciones.marcarMostrada(app, idAviso)
                    terminar()
                } else {
                    emitir(app, intent, uid, charlaId, idAviso, inicioMillis, ahora, terminar)
                }
            }
        }

        FirebaseFirestore.getInstance()
            .collection("notificaciones")
            .document(idAviso)
            .get()
            .addOnSuccessListener { documento ->
                decidir(documento.getBoolean("notificada") == true)
            }
            // sin red y sin cache: mejor avisar de mas que no avisar
            .addOnFailureListener { decidir(false) }

        val principal = Handler(Looper.getMainLooper())
        principal.postDelayed({ decidir(false) }, ESPERA_CONSULTA_MS)
        principal.postDelayed(terminar, ESPERA_MAXIMA_MS)
    }

    private fun emitir(
        context: Context,
        intent: Intent,
        uid: String,
        charlaId: String,
        idAviso: String,
        inicioMillis: Long,
        ahora: Long,
        terminar: () -> Unit
    ) {
        val titulo = intent.getStringExtra(EXTRA_TITULO).orEmpty()
        val lugar = intent.getStringExtra(EXTRA_LUGAR)
        val hora = intent.getStringExtra(EXTRA_HORA).orEmpty()

        val tituloAviso = "${cuantoFalta(inicioMillis, ahora)}: $titulo"
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
        RegistroNotificaciones.marcarMostrada(context, idAviso)

        guardarEnHistorial(uid, charlaId, idAviso, tituloAviso, mensaje, terminar)
    }

    /**
     * Escribe el doc del recordatorio. Sin red la escritura igual queda en la
     * cache local y se sube cuando vuelve la conexion, por eso alcanza con
     * esperar un tope corto y no hasta que el servidor confirme.
     */
    private fun guardarEnHistorial(
        uid: String,
        charlaId: String,
        idAviso: String,
        titulo: String,
        mensaje: String,
        terminar: () -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("notificaciones")
            .document(idAviso)
            .set(
                NotificacionFirebase(
                    id = idAviso,
                    destinatarioUid = uid,
                    tipo = TipoNotificacion.RECORDATORIO_CHARLA.name,
                    titulo = titulo,
                    mensaje = mensaje,
                    referenciaId = charlaId,
                    creadaEn = Timestamp.now(),
                    // la alarma ya lo acaba de mostrar: el doc es solo historial
                    notificada = true
                )
            )
            .addOnFailureListener {
                Log.e(TAG, "No se pudo guardar el recordatorio $charlaId", it)
            }
            .addOnCompleteListener { terminar() }
    }

    /** "En 30 minutos", "En 12 minutos", "En 1 minuto" segun cuando suene. */
    private fun cuantoFalta(inicioMillis: Long, ahora: Long): String {
        if (inicioMillis <= 0) return "En ${ProgramadorRecordatorios.MINUTOS_ANTES} minutos"

        // redondeo para arriba: a las 16:45:30 para las 17:00 faltan "15"
        val minutos =
            ((inicioMillis - ahora + 59_999) / 60_000)
                .coerceIn(1, ProgramadorRecordatorios.MINUTOS_ANTES)

        return if (minutos == 1L) "En 1 minuto" else "En $minutos minutos"
    }

    companion object {
        const val EXTRA_CHARLA_ID = "charlaId"
        const val EXTRA_TITULO = "titulo"
        const val EXTRA_LUGAR = "lugar"
        const val EXTRA_HORA = "hora"
        /** Inicio de la charla en epoch millis; define la identidad del aviso. */
        const val EXTRA_INICIO_MILLIS = "inicioMillis"

        private const val TAG = "RecordatorioCharla"

        /** Cuanto se espera la consulta a Firestore antes de avisar igual. */
        private const val ESPERA_CONSULTA_MS = 4_000L

        /** Por debajo de los ~10 s que Android le da a un receiver. */
        private const val ESPERA_MAXIMA_MS = 8_000L

        private val FORMATO_ID: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm")

        /**
         * Id del doc de historial, y del aviso en el registro local. Incluye el
         * horario: si el organizador mueve la charla, es un aviso nuevo y
         * vuelve a sonar; si no la mueve, nunca se repite.
         */
        fun idHistorial(uid: String, charlaId: String, inicio: LocalDateTime): String =
            "recordatorio_${uid}_${charlaId}_${inicio.format(FORMATO_ID)}"

        private fun aLocalDateTime(millis: Long): LocalDateTime =
            Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }
}
