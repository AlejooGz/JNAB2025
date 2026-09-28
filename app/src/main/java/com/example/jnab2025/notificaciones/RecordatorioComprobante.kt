package com.example.jnab2025.notificaciones

import android.content.Context
import android.util.Log
import com.example.jnab2025.data.model.ComprobanteFirebase
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.InscripcionFirebase
import com.example.jnab2025.data.model.NotificacionFirebase
import com.example.jnab2025.data.model.TipoNotificacion
import com.example.jnab2025.data.model.TrabajoFirebase
import com.example.jnab2025.utils.Sesion
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Recordatorio al expositor que envio trabajos pero todavia no tiene un
 * comprobante de pago en revision o acreditado: sin eso la organizacion no
 * puede programar su presentacion.
 *
 * A diferencia de los demas avisos, este se repite a proposito: suena cada vez
 * que el expositor inicia sesion o abre la app en frio, hasta que carga el
 * comprobante. Lo arma el propio dispositivo (no hay nadie que lo "envie").
 *
 * En la pantalla de notificaciones hay UN solo doc por expositor, que se
 * reescribe en cada recordatorio: vuelve a quedar sin leer y sube arriba, sin
 * llenar el historial de copias.
 */
object RecordatorioComprobante {

    private const val TAG = "RecordatorioComprobante"

    /** Id fijo del aviso en la bandeja: cada recordatorio reemplaza al anterior. */
    private const val ID_NOTIFICACION = 90_001

    /**
     * Usuario al que ya se le recordo en este proceso. Vive en el object (y no
     * en la Activity) para que rotar la pantalla no lo haga sonar de nuevo;
     * se reinicia al cerrar sesion y al matar la app, que es cuando tiene que
     * volver a sonar.
     */
    private var uidRecordado: String? = null

    /** Lo llama MainActivity al quedar con sesion (login o app abierta en frio). */
    fun revisar(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        if (!Sesion.esExpositor(context)) return
        if (uid == uidRecordado) return
        uidRecordado = uid

        val app = context.applicationContext
        val firestore = FirebaseFirestore.getInstance()

        // la inscripcion y su comprobante usan el UID como id de documento
        val trabajos = firestore.collection("trabajos").whereEqualTo("autorUid", uid).get()
        val inscripcion = firestore.collection("inscripciones").document(uid).get()
        val comprobante = firestore.collection("comprobantes").document(uid).get()

        Tasks.whenAllComplete(trabajos, inscripcion, comprobante)
            .addOnSuccessListener {
                // sin datos confiables no se molesta al usuario
                if (!trabajos.isSuccessful || !inscripcion.isSuccessful || !comprobante.isSuccessful) {
                    Log.e(TAG, "No se pudo revisar si falta el comprobante")
                    return@addOnSuccessListener
                }

                val tieneTrabajos =
                    trabajos.result.documents
                        .mapNotNull { it.toObject(TrabajoFirebase::class.java) }
                        .any { it.estado != EstadoTrabajo.RECHAZADO.name }
                if (!tieneTrabajos) return@addOnSuccessListener

                val mensaje =
                    mensajeSegunSituacion(
                        inscripcion.result.toObject(InscripcionFirebase::class.java),
                        comprobante.result.toObject(ComprobanteFirebase::class.java)
                    ) ?: return@addOnSuccessListener

                avisar(app, uid, mensaje)
            }
    }

    /** Al cerrar sesion: el proximo login tiene que volver a revisar. */
    fun reiniciar() {
        uidRecordado = null
    }

    /**
     * Que decirle segun en que punto esta, o null si no hace falta
     * recordarle nada (comprobante en revision, acreditado, o inscripcion
     * pagada/anulada).
     */
    private fun mensajeSegunSituacion(
        inscripcion: InscripcionFirebase?,
        comprobante: ComprobanteFirebase?
    ): String? {
        val cierre = " para que la organización pueda programar tu presentación."

        if (inscripcion == null) {
            return "Todavía no te inscribiste a las Jornadas. Inscribite y cargá tu comprobante de pago$cierre"
        }
        if (inscripcion.estado != EstadoInscripcion.PENDIENTE_PAGO.name) return null

        return when (comprobante?.estado) {
            null -> "Cargá tu comprobante de pago$cierre"
            EstadoComprobante.RECHAZADO.name ->
                "Tu comprobante fue rechazado" +
                        (comprobante.motivoRechazo?.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: "") +
                        ". Cargá uno nuevo$cierre"
            // PENDIENTE (en revision) o VERIFICADO: ya no hay nada que hacer
            else -> null
        }
    }

    private fun avisar(context: Context, uid: String, mensaje: String) {
        val titulo = "Falta tu comprobante de pago"

        Notificaciones.mostrar(
            context = context,
            id = ID_NOTIFICACION,
            canal = Notificaciones.CANAL_PAGOS,
            titulo = titulo,
            mensaje = mensaje,
            tipo = TipoNotificacion.RECORDATORIO_COMPROBANTE.name
        )

        // Una sola entrada en la pantalla de notificaciones: se reescribe
        // entera, asi vuelve a quedar sin leer y con la fecha de ahora.
        // notificada = true porque ya se acaba de mostrar en la bandeja.
        val id = "recordatorio_comprobante_$uid"
        FirebaseFirestore.getInstance()
            .collection("notificaciones")
            .document(id)
            .set(
                NotificacionFirebase(
                    id = id,
                    destinatarioUid = uid,
                    tipo = TipoNotificacion.RECORDATORIO_COMPROBANTE.name,
                    titulo = titulo,
                    mensaje = mensaje,
                    referenciaId = "",
                    creadaEn = Timestamp.now(),
                    leida = false,
                    notificada = true
                )
            )
            .addOnFailureListener {
                Log.e(TAG, "No se pudo guardar el recordatorio de comprobante", it)
            }
    }
}
