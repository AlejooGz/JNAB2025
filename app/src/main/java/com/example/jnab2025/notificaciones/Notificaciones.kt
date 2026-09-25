package com.example.jnab2025.notificaciones

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.jnab2025.MainActivity
import com.example.jnab2025.R

/**
 * Punto unico por el que pasan todas las notificaciones de la app.
 *
 * Los canales se crean de forma idempotente antes de cada aviso: no hay clase
 * Application donde hacerlo una sola vez, y los avisos tambien salen desde un
 * BroadcastReceiver y desde un Worker, que corren sin que la UI haya arrancado.
 */
object Notificaciones {

    const val CANAL_PAGOS = "jnab_pagos"
    const val CANAL_RECORDATORIOS = "jnab_recordatorios"
    const val CANAL_LUGARES = "jnab_lugares"
    const val CANAL_TRABAJOS = "jnab_trabajos"

    /**
     * Extras del intent que abre la app al tocar el aviso. MainActivity los lee
     * para llevar a la pantalla del aviso (el mapa en un lugar, las propuestas
     * de un simposio, Mis trabajos).
     */
    const val EXTRA_TIPO = "notificacion_tipo"
    const val EXTRA_REFERENCIA_ID = "notificacion_referencia_id"

    /** Base para los ids de recordatorio, para no pisar los avisos de pago. */
    private const val BASE_ID_RECORDATORIO = 100_000

    fun crearCanales(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager =
            context.getSystemService(NotificationManager::class.java)
                ?: return

        manager.createNotificationChannel(
            NotificationChannel(
                CANAL_PAGOS,
                "Pagos e inscripciones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Avisos sobre el estado de tu comprobante e inscripcion"
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CANAL_RECORDATORIOS,
                "Recordatorios de charlas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Te avisa 30 minutos antes de cada charla de tu agenda"
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CANAL_LUGARES,
                "Novedades del mapa",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Avisa cuando se agrega un lugar con descuento al mapa"
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CANAL_TRABAJOS,
                "Trabajos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description =
                    "Envios de trabajos a tus simposios y resoluciones de tus trabajos"
            }
        )
    }

    /**
     * En Android 13+ la notificacion se descarta en silencio si falta el
     * permiso, asi que conviene preguntar antes de armarla.
     */
    fun puedeNotificar(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val concedido =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!concedido) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun idRecordatorio(charlaId: String): Int =
        BASE_ID_RECORDATORIO + charlaId.hashCode().rem(10_000)

    /*
     * puedeNotificar() ya corta si falta POST_NOTIFICATIONS, pero lint no puede
     * seguir el chequeo a traves de la llamada y marca notify() como
     * MissingPermission. El runCatching cubre ademas el SecurityException que
     * podria tirar un fabricante con su propia gestion de permisos.
     */
    @SuppressLint("MissingPermission")
    fun mostrar(
        context: Context,
        id: Int,
        canal: String,
        titulo: String,
        mensaje: String,
        tipo: String? = null,
        referenciaId: String? = null
    ) {
        if (!puedeNotificar(context)) return

        crearCanales(context)

        // Al tocar el aviso se abre la app reusando la instancia que ya exista.
        // SINGLE_TOP hace que esa instancia reciba el intent en onNewIntent en
        // vez de recrearse, asi no se pierde la pantalla en la que estaba.
        val intent =
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                if (tipo != null) putExtra(EXTRA_TIPO, tipo)
                if (referenciaId != null) putExtra(EXTRA_REFERENCIA_ID, referenciaId)
            }

        val pendiente =
            PendingIntent.getActivity(
                context,
                id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val aviso =
            NotificationCompat.Builder(context, canal)
                .setSmallIcon(R.drawable.outline_breaking_news_24)
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText(mensaje)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendiente)
                .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(id, aviso)
        }
    }
}
