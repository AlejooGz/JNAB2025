package com.example.jnab2025.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Las alarmas programadas se borran al apagar el telefono. Al volver a
 * arrancar, este receiver encola el worker que las vuelve a crear leyendo la
 * agenda del usuario desde Firestore.
 */
class ArranqueReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        NotificacionesWorker.ejecutarAhora(context)
    }
}
