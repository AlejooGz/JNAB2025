package com.example.jnab2025.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel.CharlaProxima
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel.NovedadResumen
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel.SituacionInscripcion
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

/*
 * Secciones del home del asistente. Estan separadas de la pantalla porque el
 * home del expositor tambien las muestra: se reutilizan tal cual, solo
 * llamando a la funcion.
 */

/**
 * Lo que las secciones pueden pedir que se haga al tocarlas. La pantalla no
 * sabe navegar (no conoce el NavController): solo avisa "tocaron esto" y el
 * Fragment decide a donde ir.
 */
data class AccionesAsistente(
    val abrirInscripcion: () -> Unit = {},
    val abrirComprobante: () -> Unit = {},
    val abrirCharla: (charlaId: String) -> Unit = {},
    val abrirCronograma: () -> Unit = {},
    val abrirNovedades: () -> Unit = {},
    val abrirMapa: () -> Unit = {},
    val abrirFaq: () -> Unit = {}
)

private val localeAr: Locale = Locale.forLanguageTag("es-AR")
internal val formatoDia: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE dd/MM", localeAr)
private val formatoFechaCompleta: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/**
 * "Faltan N dias para las Jornadas". [hoy] es parametro (y no LocalDate.now()
 * adentro) para poder probar cada caso desde un @Preview.
 */
@Composable
fun SeccionCuentaRegresiva(
    inicio: LocalDate?,
    fin: LocalDate?,
    hoy: LocalDate = LocalDate.now()
) {
    // sin charlas cargadas no hay fecha de la que contar: no se muestra nada
    if (inicio == null || fin == null) return

    val (titulo, detalle) = when {
        hoy.isBefore(inicio) -> {
            val dias = ChronoUnit.DAYS.between(hoy, inicio)
            val titulo = if (dias == 1L) "¡Mañana empiezan las Jornadas!"
            else "Faltan $dias días para las Jornadas"
            titulo to "Comienzan el ${inicio.format(formatoFechaCompleta)}"
        }
        !hoy.isAfter(fin) -> {
            val diaActual = ChronoUnit.DAYS.between(inicio, hoy) + 1
            val totalDias = ChronoUnit.DAYS.between(inicio, fin) + 1
            "¡Las Jornadas están en curso!" to "Día $diaActual de $totalDias"
        }
        else -> "Las Jornadas finalizaron" to "¡Gracias por participar!"
    }

    TarjetaJnab(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 16.dp)
    ) {
        Text(
            text = titulo,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = detalle,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}

/** Como se dibuja cada situacion: color del punto, mensaje y boton (si hay). */
private data class Vista(
    val color: Color,
    val mensaje: String,
    val boton: String?,
    val onBoton: () -> Unit
)

@Composable
fun SeccionInscripcion(
    situacion: SituacionInscripcion,
    acciones: AccionesAsistente
) {
    TituloSeccion("Mi inscripción")

    val verde = MaterialTheme.colorScheme.tertiary
    val rojo = MaterialTheme.colorScheme.primary
    val ambar = Color(0xFFE0A100)

    val vista = when (situacion) {
        SituacionInscripcion.SinInscripcion -> Vista(
            ambar, "Todavía no estás inscripto a las Jornadas.",
            "Inscribirme", acciones.abrirInscripcion
        )
        is SituacionInscripcion.FaltaPago -> Vista(
            ambar, "Tu inscripción está registrada. Falta acreditar el pago de $${situacion.monto.toInt()}.",
            "Cargar comprobante", acciones.abrirComprobante
        )
        SituacionInscripcion.ComprobanteEnRevision -> Vista(
            ambar, "Tu comprobante está en revisión. Te avisamos cuando la organización lo verifique.",
            null, {}
        )
        is SituacionInscripcion.ComprobanteRechazado -> Vista(
            rojo,
            "Tu comprobante fue rechazado" +
                (situacion.motivo?.let { ": $it" } ?: "") + ". Cargá uno nuevo.",
            "Cargar otro comprobante", acciones.abrirComprobante
        )
        SituacionInscripcion.Confirmada -> Vista(
            verde, "¡Tu inscripción está confirmada! No tenés nada pendiente.",
            null, {}
        )
        SituacionInscripcion.Anulada -> Vista(
            rojo, "Tu inscripción figura anulada. Contactate con la organización.",
            null, {}
        )
    }

    TarjetaJnab(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = acciones.abrirInscripcion
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(vista.color, CircleShape)
            )
            Text(
                text = vista.mensaje,
                fontSize = 15.sp,
                modifier = Modifier.padding(start = 10.dp)
            )
        }
        if (vista.boton != null) {
            Button(
                onClick = vista.onBoton,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(vista.boton)
            }
        }
    }
}

@Composable
fun SeccionProximasCharlas(
    charlas: List<CharlaProxima>,
    acciones: AccionesAsistente
) {
    TituloSeccion("Próximas charlas de mi agenda")

    TarjetaJnab(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        if (charlas.isEmpty()) {
            TextoVacio("No tenés charlas próximas en tu agenda. Marcá las que te interesen desde el cronograma.")
        }
        // forEachIndexed: para poner un separador entre renglones, no despues del ultimo
        charlas.forEachIndexed { indice, charla ->
            if (indice > 0) HorizontalDivider()
            RenglonLista(
                titulo = charla.titulo,
                detalle = "${charla.fecha.format(formatoDia)} · ${charla.horaInicio} a ${charla.horaFin}",
                extra = charla.aula?.let { "Aula: $it" },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { acciones.abrirCharla(charla.charlaId) }
            )
        }
        EnlaceTarjeta("Ir al cronograma", acciones.abrirCronograma)
    }
}

@Composable
fun SeccionNovedades(
    novedades: List<NovedadResumen>,
    acciones: AccionesAsistente
) {
    TituloSeccion("Últimas novedades")

    TarjetaJnab(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = acciones.abrirNovedades
    ) {
        if (novedades.isEmpty()) {
            TextoVacio("Todavía no hay novedades publicadas.")
        }
        novedades.forEachIndexed { indice, novedad ->
            if (indice > 0) HorizontalDivider()
            RenglonLista(
                titulo = novedad.titulo,
                detalle = novedad.fecha?.format(formatoFechaCompleta) ?: ""
            )
        }
        EnlaceTarjeta("Ver todas", acciones.abrirNovedades)
    }
}
