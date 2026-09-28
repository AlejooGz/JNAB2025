package com.example.jnab2025.notificaciones

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime
import java.time.ZoneId

/**
 * Programa, con AlarmManager, un aviso 30 minutos antes de cada charla que el
 * usuario tenga en su agenda. Si ya estamos dentro de esa media hora (se
 * inicio sesion o se abrio la app tarde) y el aviso todavia no salio, se
 * programa para ya: la media hora entera es la ventana para avisar.
 *
 * Se resuelve entero en el dispositivo a proposito: el horario de la charla se
 * conoce de antemano, asi que no hace falta que nadie mande un push. La alarma
 * sobrevive a que se cierre la app (la guarda el sistema), pero no a un
 * reinicio ni a un "forzar detencion": de eso se ocupan [ArranqueReceiver] y
 * el chequeo periodico de [NotificacionesWorker].
 */
object ProgramadorRecordatorios {

    private const val TAG = "Recordatorios"

    /** Cuanto antes de que empiece la charla avisamos. */
    const val MINUTOS_ANTES = 30L

    /** Charla lista para programar, ya resuelta a fecha y hora concretas. */
    data class Programable(
        val charlaId: String,
        val titulo: String,
        val inicio: LocalDateTime,
        val lugar: String? = null
    )

    /**
     * Deja programado exactamente el conjunto recibido: cancela lo que estaba
     * de antes y crea las alarmas nuevas. Se llama cada vez que la agenda
     * cambia, asi que tiene que ser idempotente.
     */
    fun reprogramar(context: Context, charlas: List<Programable>) {
        val app = context.applicationContext

        cancelarTodos(app)

        val ahora = LocalDateTime.now()
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        val programadas = mutableSetOf<String>()

        charlas.forEach { charla ->
            // Una charla que ya empezo no tiene nada que avisar.
            if (!charla.inicio.isAfter(ahora)) return@forEach

            val momentoAviso = charla.inicio.minusMinutes(MINUTOS_ANTES)

            val cuando =
                if (momentoAviso.isAfter(ahora)) {
                    momentoAviso
                } else {
                    // Estamos dentro de la media hora previa. Si el aviso ya
                    // salio en este dispositivo no se reprograma; si salio en
                    // otro, lo frena el receiver al consultar Firestore.
                    if (uid == null) return@forEach
                    val idAviso =
                        RecordatorioCharlaReceiver.idHistorial(uid, charla.charlaId, charla.inicio)
                    if (RegistroNotificaciones.yaMostrada(app, idAviso)) return@forEach
                    // una alarma en el pasado AlarmManager la dispara enseguida
                    ahora
                }

            if (programar(app, charla, cuando)) {
                programadas += charla.charlaId
            }
        }

        RegistroNotificaciones.guardarProgramadas(app, programadas)
        Log.d(TAG, "Recordatorios programados: ${programadas.size}")
    }

    private fun programar(
        context: Context,
        charla: Programable,
        momentoAviso: LocalDateTime
    ): Boolean {
        val alarmManager =
            context.getSystemService(AlarmManager::class.java) ?: return false

        val enMillis =
            momentoAviso
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

        val intent =
            Intent(context, RecordatorioCharlaReceiver::class.java).apply {
                putExtra(
                    RecordatorioCharlaReceiver.EXTRA_CHARLA_ID,
                    charla.charlaId
                )
                putExtra(
                    RecordatorioCharlaReceiver.EXTRA_TITULO,
                    charla.titulo
                )
                putExtra(
                    RecordatorioCharlaReceiver.EXTRA_LUGAR,
                    charla.lugar
                )
                putExtra(
                    RecordatorioCharlaReceiver.EXTRA_HORA,
                    charla.inicio.toLocalTime().toString()
                )
                putExtra(
                    RecordatorioCharlaReceiver.EXTRA_INICIO_MILLIS,
                    charla.inicio
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                )
            }

        val pendiente = pendingIntent(context, charla.charlaId, intent)

        return runCatching {
            if (puedeAlarmasExactas(alarmManager)) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    enMillis,
                    pendiente
                )
            } else {
                // Sin permiso de alarma exacta el aviso puede correrse unos
                // minutos. Preferible eso a no avisar.
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    enMillis,
                    pendiente
                )
            }
            true
        }.onFailure {
            Log.e(TAG, "No se pudo programar ${charla.charlaId}", it)
        }.getOrDefault(false)
    }

    fun cancelarTodos(context: Context) {
        val app = context.applicationContext
        val alarmManager =
            app.getSystemService(AlarmManager::class.java) ?: return

        RegistroNotificaciones.charlasProgramadas(app).forEach { charlaId ->
            val intent = Intent(app, RecordatorioCharlaReceiver::class.java)
            alarmManager.cancel(pendingIntent(app, charlaId, intent))
        }

        RegistroNotificaciones.guardarProgramadas(app, emptySet())
    }

    /**
     * El requestCode define la identidad de la alarma: mismo charlaId, misma
     * alarma, y por eso reprogramar no duplica avisos.
     */
    private fun pendingIntent(
        context: Context,
        charlaId: String,
        intent: Intent
    ): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            Notificaciones.idRecordatorio(charlaId),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    private fun puedeAlarmasExactas(alarmManager: AlarmManager): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
}
