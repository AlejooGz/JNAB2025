package com.example.jnab2025.ui.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel.CharlaProxima
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
    val abrirCronograma: () -> Unit = {}
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
