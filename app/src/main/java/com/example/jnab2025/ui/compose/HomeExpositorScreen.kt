package com.example.jnab2025.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jnab2025.R
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel
import com.example.jnab2025.ui.viewmodels.HomeExpositorViewModel
import com.example.jnab2025.ui.viewmodels.HomeExpositorViewModel.Presentacion
import com.example.jnab2025.ui.viewmodels.HomeExpositorViewModel.TrabajoResuelto
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Acciones propias del expositor; las compartidas van en [AccionesAsistente]. */
data class AccionesExpositor(
    val abrirMisTrabajos: () -> Unit = {},
    val enviarTrabajo: () -> Unit = {}
)

/**
 * Home del expositor: arriba lo propio (trabajos y presentaciones) y abajo
 * las mismas secciones que ve el asistente, reutilizadas sin copiar codigo.
 * Recibe dos estados porque los datos vienen de dos ViewModels.
 */
@Composable
fun HomeExpositorScreen(
    nombre: String,
    estado: HomeExpositorViewModel.Estado,
    estadoAsistente: HomeAsistenteViewModel.Estado,
    acciones: AccionesExpositor,
    accionesAsistente: AccionesAsistente
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoHome(
            saludo = "¡Hola, $nombre!",
            subtitulo = "Panel del expositor"
        )

        if (estado.cargando || estadoAsistente.cargando) {
            IndicadorCarga()
            return@Column
        }

        SeccionCuentaRegresiva(estadoAsistente.inicioJornadas, estadoAsistente.finJornadas)
        AvisoPendientes(estado, accionesAsistente)
        ResumenTrabajos(estado, acciones)
        SeccionPresentaciones(estado.presentaciones, accionesAsistente)
        SeccionResueltos(estado.resueltos, acciones)

        // --- secciones reutilizadas del home del asistente ---
        SeccionInscripcion(estadoAsistente.inscripcion, accionesAsistente)
        SeccionProximasCharlas(estadoAsistente.proximasCharlas, accionesAsistente)
        SeccionNovedades(estadoAsistente.novedades, accionesAsistente)

        AccesosExpositor(acciones, accionesAsistente)
        EspacioFinal()
    }
}

/**
 * Banner destacado cuando hay algo que el expositor puede destrabar.
 * Si no hay nada pendiente, no dibuja nada (la funcion retorna sin emitir UI).
 */
@Composable
private fun AvisoPendientes(
    estado: HomeExpositorViewModel.Estado,
    accionesAsistente: AccionesAsistente
) {
    if (estado.pendientesDePago == 0 && estado.sinProgramar == 0) return

    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Para tener en cuenta", fontWeight = FontWeight.Bold, fontSize = 17.sp)

            if (estado.pendientesDePago > 0) {
                val n = estado.pendientesDePago
                Text(
                    text = if (n == 1) {
                        "Tenés un trabajo aceptado. Se programa cuando se acredite tu inscripción."
                    } else {
                        "Tenés $n trabajos aceptados. Se programan cuando se acredite tu inscripción."
                    },
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Button(
                    onClick = accionesAsistente.abrirInscripcion,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Ir a mi inscripción")
                }
            }

            if (estado.sinProgramar > 0) {
                val n = estado.sinProgramar
                Text(
                    text = if (n == 1) {
                        "Un trabajo aprobado está esperando que la organización le asigne horario."
                    } else {
                        "$n trabajos aprobados están esperando que la organización les asigne horario."
                    },
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ResumenTrabajos(
    estado: HomeExpositorViewModel.Estado,
    acciones: AccionesExpositor
) {
    TituloSeccion("Mis trabajos")
    FilaDeTarjetas {
        TarjetaContador(estado.enRevision, "En revisión", "Ver", acciones.abrirMisTrabajos, Modifier.weight(1f))
        TarjetaContador(estado.aceptados, "Aceptados", "Ver", acciones.abrirMisTrabajos, Modifier.weight(1f))
        TarjetaContador(estado.rechazados, "Rechazados", "Ver", acciones.abrirMisTrabajos, Modifier.weight(1f))
    }
}

@Composable
private fun SeccionPresentaciones(
    presentaciones: List<Presentacion>,
    accionesAsistente: AccionesAsistente
) {
    TituloSeccion("Mis presentaciones")
    TarjetaJnab(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        if (presentaciones.isEmpty()) {
            TextoVacio("Todavía no tenés presentaciones programadas.")
        }
        presentaciones.forEachIndexed { indice, presentacion ->
            if (indice > 0) HorizontalDivider()
            RenglonLista(
                titulo = presentacion.titulo,
                detalle = "${presentacion.fecha.format(formatoDia)} · " +
                    "${presentacion.horaInicio} a ${presentacion.horaFin}",
                extra = listOfNotNull(
                    presentacion.aula?.let { "Aula: $it" },
                    presentacion.simposio
                ).joinToString(" · ").ifBlank { null },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { accionesAsistente.abrirCharla(presentacion.charlaId) }
            )
        }
    }
}

private val formatoResolucion: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

@Composable
private fun SeccionResueltos(
    resueltos: List<TrabajoResuelto>,
    acciones: AccionesExpositor
) {
    TituloSeccion("Últimas resoluciones")
    TarjetaJnab(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = acciones.abrirMisTrabajos
    ) {
        if (resueltos.isEmpty()) {
            TextoVacio("La organización todavía no resolvió ninguno de tus trabajos.")
        }
        resueltos.forEachIndexed { indice, trabajo ->
            if (indice > 0) HorizontalDivider()
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                Text(trabajo.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                val (texto, color) = etiquetaEstado(trabajo.estado)
                Text(
                    text = texto + (trabajo.fecha?.let { " · ${it.format(formatoResolucion)}" } ?: ""),
                    color = color,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                if (trabajo.estado == EstadoTrabajo.RECHAZADO && !trabajo.motivoRechazo.isNullOrBlank()) {
                    Text("Motivo: ${trabajo.motivoRechazo}", fontSize = 13.sp)
                }
            }
        }
        EnlaceTarjeta("Ver mis trabajos", acciones.abrirMisTrabajos)
    }
}

@Composable
private fun etiquetaEstado(estado: EstadoTrabajo): Pair<String, Color> = when (estado) {
    EstadoTrabajo.APROBADO -> "Aprobado" to MaterialTheme.colorScheme.tertiary
    EstadoTrabajo.ACEPTADO_PENDIENTE_PAGO -> "Aceptado · falta el pago" to Color(0xFFB07800)
    EstadoTrabajo.RECHAZADO -> "Rechazado" to MaterialTheme.colorScheme.primary
    else -> "En revisión" to MaterialTheme.colorScheme.onSurface
}

@Composable
private fun AccesosExpositor(
    acciones: AccionesExpositor,
    accionesAsistente: AccionesAsistente
) {
    TituloSeccion("Accesos rápidos")
    FilaDeTarjetas {
        AccesoRapido(R.drawable.outline_list_alt_add_24, "Enviar trabajo", acciones.enviarTrabajo, Modifier.weight(1f))
        AccesoRapido(R.drawable.outline_edit_document_24, "Mis trabajos", acciones.abrirMisTrabajos, Modifier.weight(1f))
    }
    EspacioEntreFilas()
    FilaDeTarjetas {
        AccesoRapido(R.drawable.outline_event_available_24, "Cronograma", accionesAsistente.abrirCronograma, Modifier.weight(1f))
        AccesoRapido(R.drawable.outline_check_circle_24, "Inscripción", accionesAsistente.abrirInscripcion, Modifier.weight(1f))
    }
}

/* --------------------------------- Previews --------------------------------- */

private val estadoExpositorEjemplo = HomeExpositorViewModel.Estado(
    cargando = false,
    enRevision = 1,
    aceptados = 2,
    rechazados = 1,
    pendientesDePago = 1,
    sinProgramar = 0,
    presentaciones = listOf(
        Presentacion(
            "c1", "Paleodemografía de sitios del Delta del Paraná",
            LocalDate.now().plusDays(13), LocalTime.of(15, 0), LocalTime.of(15, 30),
            "Aula 2", "Bioarqueología"
        )
    ),
    resueltos = listOf(
        TrabajoResuelto(
            "t1", "Paleodemografía de sitios del Delta del Paraná",
            EstadoTrabajo.APROBADO, null, LocalDate.now().minusDays(3)
        ),
        TrabajoResuelto(
            "t2", "Estatura y nutrición en escolares",
            EstadoTrabajo.RECHAZADO, "El resumen excede las 300 palabras", LocalDate.now().minusDays(6)
        )
    )
)

@Preview(name = "Home expositor", showBackground = true, heightDp = 2200)
@Composable
private fun PreviewHomeExpositor() {
    TemaJnab {
        HomeExpositorScreen(
            nombre = "Martín",
            estado = estadoExpositorEjemplo,
            estadoAsistente = estadoAsistenteEjemplo,
            acciones = AccionesExpositor(),
            accionesAsistente = AccionesAsistente()
        )
    }
}
