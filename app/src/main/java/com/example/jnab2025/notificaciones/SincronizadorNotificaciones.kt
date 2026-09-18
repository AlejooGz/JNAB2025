package com.example.jnab2025.notificaciones

import android.content.Context
import android.util.Log
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.data.model.NotificacionFirebase
import com.example.jnab2025.data.model.SimposioFirebase
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Version bloqueante de las dos tareas de notificaciones, pensada para correr
 * fuera de la UI (worker y arranque del telefono), donde no hay ViewModel vivo
 * ni conviene depender de listeners.
 *
 * Nunca debe llamarse desde el hilo principal: usa [Tasks.await].
 */
object SincronizadorNotificaciones {

    private const val TAG = "SincroNotif"

    /** Corta la espera para no colgar al worker si no hay red. */
    private const val ESPERA_SEGUNDOS = 20L

    /**
     * Emite los avisos que hayan quedado pendientes mientras la app estaba
     * cerrada. El listener en vivo cubre el caso de la app abierta.
     */
    fun revisarPendientes(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        runCatching {
            val snapshot =
                Tasks.await(
                    FirebaseFirestore.getInstance()
                        .collection("notificaciones")
                        .whereEqualTo("destinatarioUid", uid)
                        .get(),
                    ESPERA_SEGUNDOS,
                    java.util.concurrent.TimeUnit.SECONDS
                )

            snapshot.documents
                .mapNotNull { it.toObject(NotificacionFirebase::class.java) }
                .forEach { notificacion ->
                    emitirSiEsNueva(context, notificacion)
                }
        }.onFailure {
            Log.e(TAG, "No se pudieron revisar las notificaciones", it)
        }
    }

    /**
     * Emite el aviso solo si este dispositivo no lo mostro antes. Compartido
     * con el listener en vivo para que ambos caminos filtren igual.
     */
    fun emitirSiEsNueva(
        context: Context,
        notificacion: NotificacionFirebase
    ) {
        if (notificacion.id.isBlank()) return
        if (RegistroNotificaciones.yaMostrada(context, notificacion.id)) return

        Notificaciones.mostrar(
            context = context,
            id = notificacion.id.hashCode(),
            canal = Notificaciones.CANAL_PAGOS,
            titulo = notificacion.titulo,
            mensaje = notificacion.mensaje
        )

        RegistroNotificaciones.marcarMostrada(context, notificacion.id)
    }

    /**
     * Reconstruye las alarmas de recordatorio leyendo la agenda del usuario.
     * Ademas de cubrir el reinicio del telefono, sirve para tomar cambios de
     * horario que haya hecho el organizador.
     */
    fun sincronizarRecordatorios(context: Context) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        runCatching {
            val agenda =
                Tasks.await(
                    firestore.collection("agendaUsuarios")
                        .whereEqualTo("usuarioUid", uid)
                        .get(),
                    ESPERA_SEGUNDOS,
                    java.util.concurrent.TimeUnit.SECONDS
                )

            val misCharlas =
                agenda.documents
                    .mapNotNull { it.getString("charlaId") }
                    .toSet()

            if (misCharlas.isEmpty()) {
                ProgramadorRecordatorios.cancelarTodos(context)
                return
            }

            val charlas =
                Tasks.await(
                    firestore.collection("charlas").get(),
                    ESPERA_SEGUNDOS,
                    java.util.concurrent.TimeUnit.SECONDS
                ).documents
                    .mapNotNull { it.toObject(CharlaFirebase::class.java) }
                    .filter { it.id in misCharlas }

            val simposios =
                Tasks.await(
                    firestore.collection("simposios").get(),
                    ESPERA_SEGUNDOS,
                    java.util.concurrent.TimeUnit.SECONDS
                ).documents
                    .mapNotNull { it.toObject(SimposioFirebase::class.java) }
                    .associateBy { it.id }

            val programables =
                charlas.mapNotNull { charla ->
                    val inicio = inicioDe(charla) ?: return@mapNotNull null

                    ProgramadorRecordatorios.Programable(
                        charlaId = charla.id,
                        titulo = charla.titulo,
                        inicio = inicio,
                        lugar = charla.simposioId
                            ?.let { simposios[it]?.aulaNombre }
                    )
                }

            ProgramadorRecordatorios.reprogramar(context, programables)
        }.onFailure {
            Log.e(TAG, "No se pudieron sincronizar los recordatorios", it)
        }
    }

    /**
     * `fecha` guarda el dia y `horaInicio` el horario como "HH:mm": hay que
     * combinarlos, igual que hace el cronograma para armar sus items.
     */
    fun inicioDe(charla: CharlaFirebase): LocalDateTime? {
        val dia =
            charla.fecha
                ?.toDate()
                ?.toInstant()
                ?.atZone(ZoneId.systemDefault())
                ?.toLocalDate()
                ?: return null

        val hora =
            runCatching { LocalTime.parse(charla.horaInicio) }
                .getOrNull()
                ?: return null

        return LocalDateTime.of(dia, hora)
    }
}
