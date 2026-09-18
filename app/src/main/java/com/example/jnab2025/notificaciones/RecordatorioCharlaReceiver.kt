package com.example.jnab2025.notificaciones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Recibe la alarma que se programo 30 minutos antes de una charla y emite el
 * aviso. Corre aunque la app este cerrada: la alarma la guarda el sistema.
 */
class RecordatorioCharlaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val charlaId = intent.getStringExtra(EXTRA_CHARLA_ID) ?: return
        val titulo = intent.getStringExtra(EXTRA_TITULO).orEmpty()
        val lugar = intent.getStringExtra(EXTRA_LUGAR)
        val hora = intent.getStringExtra(EXTRA_HORA).orEmpty()

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
            titulo = "En 30 minutos: $titulo",
            mensaje = mensaje
        )
    }

    companion object {
        const val EXTRA_CHARLA_ID = "charlaId"
        const val EXTRA_TITULO = "titulo"
        const val EXTRA_LUGAR = "lugar"
        const val EXTRA_HORA = "hora"
    }
}
