package com.example.jnab2025.notificaciones

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

/**
 * Red de contencion para cuando la app no esta abierta.
 *
 * Con la app en primer plano el listener de Firestore avisa al instante; con la
 * app cerrada no hay listener vivo, asi que este worker revisa cada tanto si
 * quedo alguna notificacion sin mostrar y, de paso, reconstruye las alarmas de
 * recordatorio por si se perdieron.
 *
 * Es un [Worker] y no un CoroutineWorker a proposito: doWork ya corre fuera del
 * hilo principal, que es lo unico que necesitan las llamadas bloqueantes del
 * sincronizador.
 */
class NotificacionesWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        SincronizadorNotificaciones.revisarPendientes(applicationContext)
        SincronizadorNotificaciones.sincronizarRecordatorios(applicationContext)
        return Result.success()
    }

    companion object {

        private const val TRABAJO_PERIODICO = "jnab_notificaciones_periodico"
        private const val TRABAJO_INMEDIATO = "jnab_notificaciones_ahora"

        /**
         * 15 minutos es el minimo que admite WorkManager para trabajo
         * periodico, asi que ese es el peor caso de demora con la app cerrada.
         */
        private const val MINUTOS_INTERVALO = 15L

        private val soloConRed =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        fun programarPeriodico(context: Context) {
            val trabajo =
                PeriodicWorkRequestBuilder<NotificacionesWorker>(
                    MINUTOS_INTERVALO,
                    TimeUnit.MINUTES
                )
                    .setConstraints(soloConRed)
                    .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    TRABAJO_PERIODICO,
                    // KEEP: si ya quedo encolado de un arranque anterior no se
                    // reinicia el contador en cada onCreate de MainActivity.
                    ExistingPeriodicWorkPolicy.KEEP,
                    trabajo
                )
        }

        /** Pasada unica: al iniciar sesion y despues de reiniciar el equipo. */
        fun ejecutarAhora(context: Context) {
            val trabajo =
                OneTimeWorkRequestBuilder<NotificacionesWorker>()
                    .setConstraints(soloConRed)
                    .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(
                    TRABAJO_INMEDIATO,
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    trabajo
                )
        }

        fun cancelar(context: Context) {
            WorkManager.getInstance(context.applicationContext)
                .cancelUniqueWork(TRABAJO_PERIODICO)
        }
    }
}
