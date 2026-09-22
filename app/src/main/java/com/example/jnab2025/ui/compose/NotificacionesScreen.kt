package com.example.jnab2025.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jnab2025.R
import com.example.jnab2025.data.model.NotificacionFirebase
import com.example.jnab2025.data.model.TipoNotificacion
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Lista de notificaciones del usuario, mas nuevas primero.
 *
 * @param notificaciones null mientras carga.
 * @param resaltadas ids que estaban sin leer al abrir la pantalla: se marcan
 *   leidas enseguida, pero se siguen destacando mientras el usuario esta aca
 *   para que sepa cuales son nuevas.
 */
@Composable
fun NotificacionesScreen(
    notificaciones: List<NotificacionFirebase>?,
    resaltadas: Set<String>,
    onAbrir: (NotificacionFirebase) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { TituloSeccion("Notificaciones") }

        when {
            notificaciones == null -> item { IndicadorCarga() }

            notificaciones.isEmpty() -> item {
                TarjetaJnab(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                ) {
                    TextoVacio("Todavía no tenés notificaciones.")
                }
            }

            else -> items(notificaciones, key = { it.id }) { notificacion ->
                TarjetaNotificacion(
                    notificacion = notificacion,
                    nueva = notificacion.id in resaltadas,
                    onClick = { onAbrir(notificacion) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaNotificacion(
    notificacion: NotificacionFirebase,
    nueva: Boolean,
    onClick: () -> Unit
) {
    TarjetaJnab(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                painter = painterResource(iconoDe(notificacion.tipo)),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 2.dp, end = 12.dp)
                    .size(26.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notificacion.titulo,
                    fontSize = 15.sp,
                    // en negrita solo las nuevas, como una bandeja de correo
                    fontWeight = if (nueva) FontWeight.Bold else FontWeight.SemiBold
                )
                Text(
                    text = notificacion.mensaje,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = fechaDe(notificacion),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (nueva) {
                // punto rojo de "nueva"
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp, top = 6.dp)
                        .size(10.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}

@DrawableRes
private fun iconoDe(tipo: String): Int =
    when (tipo) {
        TipoNotificacion.RECORDATORIO_CHARLA.name -> R.drawable.outline_event_available_24
        TipoNotificacion.PAGO_APROBADO.name -> R.drawable.outline_check_circle_24
        TipoNotificacion.LUGAR_AGREGADO.name -> R.drawable.outline_add_location_alt_24
        else -> R.drawable.outline_notifications_24
    }

private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm")

private fun fechaDe(notificacion: NotificacionFirebase): String =
    notificacion.creadaEn
        ?.toDate()
        ?.toInstant()
        ?.atZone(ZoneId.systemDefault())
        ?.format(formatoFecha)
        .orEmpty()