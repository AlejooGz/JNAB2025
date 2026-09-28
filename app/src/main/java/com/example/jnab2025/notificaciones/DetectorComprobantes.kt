package com.example.jnab2025.notificaciones

import android.content.Context
import android.util.Log
import com.example.jnab2025.data.model.ComprobanteFirebase
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.InscripcionFirebase
import com.example.jnab2025.data.model.TipoInscripcion
import com.example.jnab2025.data.model.TipoNotificacion
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Aviso COMPROBANTE_RECIBIDO, detectado desde el lado del organizador.
 *
 * El inscripto no escribe nada para nadie (no puede leer `users` para saber
 * quienes son los organizadores). En cambio, el dispositivo de cada
 * organizador mira los comprobantes PENDIENTES y, por cada uno nuevo, se deja
 * a si mismo un doc en `notificaciones`. De ahi en mas sigue el circuito de
 * siempre: el listener/worker lo emite, la campanita lo cuenta, etc.
 *
 * Lo llaman el listener de [com.example.jnab2025.ui.viewmodels.NotificacionesViewModel]
 * (app abierta) y [SincronizadorNotificaciones.detectarComprobantes] (worker).
 */
object DetectorComprobantes {

    private const val TAG = "DetectorComprobantes"

    /**
     * Revisa los comprobantes pendientes y registra un aviso por cada uno que
     * haga falta. Devuelve las escrituras lanzadas, para que el worker pueda
     * esperarlas antes de terminar.
     *
     * Solo avisa de comprobantes cargados despues de la linea de base de este
     * organizador: la primera vez, los que ya estaban pendientes no suenan.
     */
    fun procesar(
        context: Context,
        organizadorUid: String,
        comprobantes: List<ComprobanteFirebase>
    ): List<Task<Void>> {
        val app = context.applicationContext
        val lineaBase = RegistroNotificaciones.lineaBaseComprobantes(app, organizadorUid)

        return comprobantes.mapNotNull { comprobante ->
            if (comprobante.estado != EstadoComprobante.PENDIENTE.name) return@mapNotNull null
            // un organizador que tambien se inscribio no se avisa a si mismo
            if (comprobante.usuarioUid == organizadorUid) return@mapNotNull null

            val carga = comprobante.fechaCarga ?: return@mapNotNull null
            val cargaMillis = carga.toDate().time
            if (cargaMillis <= lineaBase) return@mapNotNull null

            val idAviso = idAviso(organizadorUid, comprobante, cargaMillis)
            if (RegistroNotificaciones.comprobanteDetectado(app, idAviso)) return@mapNotNull null

            registrarAviso(app, organizadorUid, idAviso, comprobante, carga)
        }
    }

    /**
     * Id fijo por organizador, comprobante y momento de carga: si el mismo
     * comprobante se reemplaza (por ejemplo tras un rechazo) es otro aviso, y
     * si dos dispositivos del organizador lo detectan, escriben el mismo doc.
     */
    private fun idAviso(
        organizadorUid: String,
        comprobante: ComprobanteFirebase,
        cargaMillis: Long
    ): String =
        "comprobante_${organizadorUid}_${comprobante.id.ifBlank { comprobante.inscripcionId }}_$cargaMillis"

    private fun registrarAviso(
        context: Context,
        organizadorUid: String,
        idAviso: String,
        comprobante: ComprobanteFirebase,
        carga: Timestamp
    ): Task<Void> {
        val firestore = FirebaseFirestore.getInstance()

        // la inscripcion tiene el nombre, el tipo y el monto para el mensaje
        val lectura: Task<DocumentSnapshot> =
            if (comprobante.inscripcionId.isBlank()) {
                Tasks.forResult(null)
            } else {
                firestore
                    .collection("inscripciones")
                    .document(comprobante.inscripcionId)
                    .get()
            }

        return lectura
            .continueWithTask { tarea ->
                val inscripcion =
                    if (tarea.isSuccessful) {
                        tarea.result?.toObject(InscripcionFirebase::class.java)
                    } else {
                        null
                    }

                /* merge y sin leida/notificada: si otro dispositivo del mismo
                 * organizador ya lo creo (y quizas lo mostro), no se pisa su
                 * estado. Al crearlo por primera vez esos campos toman su
                 * valor por defecto (false) al leerlo con toObject. */
                val datos = mapOf(
                    "id" to idAviso,
                    "destinatarioUid" to organizadorUid,
                    "tipo" to TipoNotificacion.COMPROBANTE_RECIBIDO.name,
                    "titulo" to "Nuevo comprobante de pago",
                    "mensaje" to mensaje(inscripcion),
                    "referenciaId" to comprobante.inscripcionId,
                    // la fecha de carga, igual en todos los dispositivos
                    "creadaEn" to carga
                )

                firestore
                    .collection("notificaciones")
                    .document(idAviso)
                    .set(datos, SetOptions.merge())
            }
            .addOnSuccessListener {
                RegistroNotificaciones.marcarComprobanteDetectado(context, idAviso)
            }
            .addOnFailureListener {
                Log.e(TAG, "No se pudo registrar el aviso del comprobante ${comprobante.id}", it)
            }
    }

    private fun mensaje(inscripcion: InscripcionFirebase?): String {
        if (inscripcion == null) {
            return "Un inscripto cargó su comprobante de pago. Tocá para verificarlo."
        }
        val nombre = inscripcion.usuarioNombre.ifBlank { "Un inscripto" }
        val tipo =
            if (inscripcion.tipo == TipoInscripcion.EXPOSITOR.name) "expositor" else "asistente"
        return "$nombre ($tipo) cargó su comprobante de $${inscripcion.monto.toInt()}. " +
                "Tocá para verificarlo."
    }
}
