package com.example.jnab2025.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jnab2025.R
import com.example.jnab2025.data.model.InscripcionFirebase
import com.example.jnab2025.ui.viewmodels.AcreditacionViewModel.Estado
import com.example.jnab2025.ui.viewmodels.AcreditacionViewModel.Origen
import com.example.jnab2025.ui.viewmodels.AcreditacionViewModel.Resultado
import com.example.jnab2025.ui.viewmodels.AcreditacionViewModel.Seleccion
import com.example.jnab2025.ui.viewmodels.AcreditacionViewModel.Companion.ZONA_EVENTO
import java.text.Normalizer
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/*
 * Pantalla de acreditacion del organizador: boton para escanear el QR de la
 * credencial, la tarjeta con el resultado (verde / ambar / rojo / gris) y,
 * cuando no hay nadie elegido, el buscador manual de inscriptos.
 */

private val Ambar = Color(0xFFB07800)
private val Gris = Color(0xFF6B6B6B)

private val formatoDiaAcreditacion = DateTimeFormatter.ofPattern("EEEE d/M", Locale("es", "AR"))
private val formatoDiaCortoAcreditacion = DateTimeFormatter.ofPattern("d/M")
private val formatoHoraAcreditacion = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun AcreditacionScreen(
    estado: Estado,
    onEscanear: () -> Unit,
    onElegir: (String) -> Unit,
    onAcreditar: () -> Unit,
    onVerInscriptos: () -> Unit,
    onCerrar: () -> Unit
) {
    // rememberSaveable: la busqueda sobrevive a la rotacion y a ir a Inscriptos y volver
    var busqueda by rememberSaveable { mutableStateOf("") }
    val filtrados = remember(busqueda, estado.inscriptos) {
        filtrar(estado.inscriptos, busqueda)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { Encabezado(estado) }

        if (estado.sinConexion || estado.pendientesDeSincronizar > 0) {
            item { AvisoConexion(estado) }
        }

        item {
            Button(
                onClick = onEscanear,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(56.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_qr_code_scanner_24),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(24.dp)
                )
                Text("Escanear QR", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            }
        }

        val seleccion = estado.seleccion
        if (seleccion != null) {
            item {
                TarjetaResultado(
                    seleccion = seleccion,
                    cargando = estado.cargando,
                    onAcreditar = onAcreditar,
                    onEscanear = onEscanear,
                    onVerInscriptos = onVerInscriptos,
                    onCerrar = onCerrar
                )
            }
        } else {
            item {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    label = { Text("Buscar por nombre o email") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                )
            }

            when {
                estado.cargando && estado.inscriptos.isEmpty() -> item { IndicadorCarga() }

                filtrados.isEmpty() -> item {
                    TarjetaJnab(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                    ) {
                        TextoVacio(
                            if (busqueda.isBlank()) "Todavía no hay inscriptos."
                            else "Nadie coincide con \"$busqueda\"."
                        )
                    }
                }

                else -> items(filtrados, key = { it.usuarioUid.ifBlank { it.id } }) { inscripcion ->
                    val uid = inscripcion.usuarioUid.ifBlank { inscripcion.id }
                    RenglonInscripto(
                        inscripcion = inscripcion,
                        acreditadoHoy = uid in estado.acreditadosHoy,
                        onClick = { onElegir(uid) }
                    )
                }
            }
        }
    }
}

@Composable
private fun Encabezado(estado: Estado) {
    Column {
        TituloSeccion("Acreditación")
        Text(
            text = "Hoy, ${estado.hoy.format(formatoDiaAcreditacion)} · " +
                    "${estado.acreditadosHoy.size} de ${estado.inscriptos.size} acreditados",
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
    }
}

@Composable
private fun AvisoConexion(estado: Estado) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Ambar.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (estado.sinConexion) {
                Text(
                    text = "Sin conexión: se usan los datos guardados en este celular.",
                    color = Ambar,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
            if (estado.pendientesDeSincronizar > 0) {
                val n = estado.pendientesDeSincronizar
                Text(
                    text = if (n == 1) "1 ingreso pendiente de subir."
                    else "$n ingresos pendientes de subir.",
                    color = Ambar,
                    fontSize = 14.sp
                )
            }
        }
    }
}

/** Como se ve cada resultado: color, titulo y texto explicativo. */
private data class Apariencia(val color: Color, val titulo: String, val detalle: String)

@Composable
private fun aparienciaDe(resultado: Resultado, cargando: Boolean): Apariencia {
    val verde = MaterialTheme.colorScheme.tertiary
    val rojo = MaterialTheme.colorScheme.primary

    return when (resultado) {
        is Resultado.ParaAcreditar -> Apariencia(
            verde, "ACREDITAR", "Inscripción y pago verificados."
        )
        is Resultado.YaIngresoHoy -> {
            val acreditacion = resultado.acreditacion
            val hora = acreditacion.fechaHora?.let {
                Instant.ofEpochSecond(it.seconds, it.nanoseconds.toLong())
                    .atZone(ZONA_EVENTO)
                    .format(formatoHoraAcreditacion)
            }
            val quien = acreditacion.acreditadoPorNombre.takeIf { it.isNotBlank() }
            Apariencia(
                Ambar,
                "YA INGRESÓ HOY",
                "Ingresó hoy" + (hora?.let { " a las $it" } ?: "") +
                        (quien?.let { " (lo acreditó $it)" } ?: "") + ". " +
                        "Si salió y volvió, puede pasar; si no, pedile el DNI."
            )
        }
        is Resultado.PagoPendiente -> Apariencia(
            Ambar,
            if (resultado.enRevision) "EN REVISIÓN" else "PAGO PENDIENTE",
            resultado.detalle + " Podés verificarlo en Inscriptos: al volver, " +
                    "esta tarjeta se actualiza sola."
        )
        is Resultado.Anulada -> Apariencia(
            rojo, "INSCRIPCIÓN ANULADA", "No puede acreditarse. Que se contacte con la organización."
        )
        Resultado.NoInscripto -> Apariencia(
            rojo, "NO INSCRIPTO", "Este QR es de una cuenta que no tiene inscripción a las Jornadas."
        )
        Resultado.QrInvalido -> Apariencia(
            rojo, "QR INVÁLIDO", "El código escaneado no es una credencial de la app."
        )
        Resultado.SinDatos -> Apariencia(
            Gris,
            "SIN DATOS",
            if (cargando) "Todavía se están cargando los inscriptos. Probá de nuevo en un momento."
            else "Sin conexión, y esta persona no está en los datos guardados. " +
                    "Volvé a buscarla cuando haya señal."
        )
    }
}

@Composable
private fun TarjetaResultado(
    seleccion: Seleccion,
    cargando: Boolean,
    onAcreditar: () -> Unit,
    onEscanear: () -> Unit,
    onVerInscriptos: () -> Unit,
    onCerrar: () -> Unit
) {
    val resultado = seleccion.resultado
    val apariencia = aparienciaDe(resultado, cargando)
    val inscripcion = inscripcionDe(resultado)
    val diasAnteriores = when (resultado) {
        is Resultado.ParaAcreditar -> resultado.diasAnteriores
        is Resultado.YaIngresoHoy -> resultado.diasAnteriores
        else -> null
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        // franja de color: lo primero que mira el organizador
        Text(
            text = apariencia.titulo,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .background(apariencia.color)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            if (inscripcion != null) {
                Text(
                    text = inscripcion.usuarioNombre.ifBlank { "(sin nombre)" },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${legible(inscripcion.tipo)} · ${legible(inscripcion.categoria)}",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (inscripcion.usuarioEmail.isNotBlank()) {
                    Text(inscripcion.usuarioEmail, fontSize = 14.sp)
                }
            }

            Text(
                text = apariencia.detalle,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = if (inscripcion != null) 10.dp else 0.dp)
            )

            if (diasAnteriores != null) {
                Text(
                    text = if (diasAnteriores.isEmpty()) "Primer día que ingresa."
                    else "Ingresó también: " + diasAnteriores.joinToString(", ") { it.format(formatoDiaCortoAcreditacion) },
                    fontSize = 13.sp,
                    color = Gris,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
            ) {
                TextButton(onClick = onCerrar) {
                    Text(
                        if (resultado is Resultado.ParaAcreditar) "Cancelar" else "Cerrar",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                when {
                    resultado is Resultado.ParaAcreditar -> BotonAccion("Acreditar", apariencia.color, onAcreditar)
                    resultado is Resultado.PagoPendiente -> BotonAccion("Ir a Inscriptos", Ambar, onVerInscriptos)
                    seleccion.origen == Origen.ESCANEO -> BotonAccion("Escanear otro", MaterialTheme.colorScheme.primary, onEscanear)
                }
            }
        }
    }
}

@Composable
private fun BotonAccion(texto: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White),
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(texto, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RenglonInscripto(
    inscripcion: InscripcionFirebase,
    acreditadoHoy: Boolean,
    onClick: () -> Unit
) {
    TarjetaJnab(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = inscripcion.usuarioNombre.ifBlank { "(sin nombre)" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${legible(inscripcion.tipo)} · ${inscripcion.usuarioEmail}",
                    fontSize = 13.sp
                )
            }
            if (acreditadoHoy) {
                Icon(
                    painter = painterResource(R.drawable.outline_check_circle_24),
                    contentDescription = "Acreditado hoy",
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun inscripcionDe(resultado: Resultado): InscripcionFirebase? = when (resultado) {
    is Resultado.ParaAcreditar -> resultado.inscripcion
    is Resultado.YaIngresoHoy -> resultado.inscripcion
    is Resultado.PagoPendiente -> resultado.inscripcion
    is Resultado.Anulada -> resultado.inscripcion
    else -> null
}

/** "ASISTENTE" -> "Asistente". */
private fun legible(valorEnum: String): String =
    valorEnum.lowercase().replaceFirstChar { it.uppercase() }

/** Busca por nombre o email sin importar mayusculas ni tildes ("gonzalez" encuentra "González"). */
private fun filtrar(inscriptos: List<InscripcionFirebase>, busqueda: String): List<InscripcionFirebase> {
    val texto = normalizar(busqueda.trim())
    if (texto.isEmpty()) return inscriptos
    return inscriptos.filter {
        normalizar(it.usuarioNombre).contains(texto) || normalizar(it.usuarioEmail).contains(texto)
    }
}

private fun normalizar(texto: String): String =
    Normalizer.normalize(texto.lowercase(), Normalizer.Form.NFD)
        .replace(Regex("\\p{Mn}+"), "")

/* --------------------------------- Previews --------------------------------- */

private val inscripcionEjemplo = InscripcionFirebase(
    id = "uid-1",
    usuarioUid = "uid-1",
    usuarioNombre = "Alejo Gonzalez",
    usuarioEmail = "alejo@ejemplo.com"
)

@Preview(name = "Resultado verde", showBackground = true)
@Composable
private fun PreviewAcreditar() {
    TemaJnab {
        AcreditacionScreen(
            estado = Estado(
                cargando = false,
                inscriptos = listOf(inscripcionEjemplo),
                seleccion = Seleccion(
                    Origen.ESCANEO,
                    Resultado.ParaAcreditar(inscripcionEjemplo, listOf(LocalDate.of(2025, 11, 10)))
                )
            ),
            onEscanear = {}, onElegir = {}, onAcreditar = {}, onVerInscriptos = {}, onCerrar = {}
        )
    }
}

@Preview(name = "Busqueda sin conexion", showBackground = true)
@Composable
private fun PreviewBusqueda() {
    TemaJnab {
        AcreditacionScreen(
            estado = Estado(
                cargando = false,
                sinConexion = true,
                pendientesDeSincronizar = 2,
                inscriptos = listOf(inscripcionEjemplo),
                acreditadosHoy = setOf("uid-1")
            ),
            onEscanear = {}, onElegir = {}, onAcreditar = {}, onVerInscriptos = {}, onCerrar = {}
        )
    }
}
