package com.example.jnab2025.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.jnab2025.R
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel.CharlaProxima
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel.NovedadResumen
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel.SituacionInscripcion
import java.time.LocalDate
import java.time.LocalTime

/**
 * Home del asistente.
 *
 * Es "stateless": no crea el ViewModel ni lee Firebase, solo dibuja el
 * [estado] que recibe. Por eso se puede previsualizar con datos inventados
 * (ver los @Preview al final) sin correr la app. Cada vez que el Fragment le
 * pasa un estado nuevo, Compose vuelve a llamar a esta funcion y actualiza
 * solo lo que cambio ("recomposicion").
 */
@Composable
fun HomeAsistenteScreen(
    nombre: String,
    estado: HomeAsistenteViewModel.Estado,
    acciones: AccionesAsistente
) {
    // Column + verticalScroll = el LinearLayout dentro de un ScrollView del XML
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        EncabezadoHome(
            saludo = "¡Hola, $nombre!",
            subtitulo = "Tu paso por las Jornadas"
        )

        // mientras llegan los primeros datos, solo la ruedita
        if (estado.cargando) {
            IndicadorCarga()
            return@Column
        }

        SeccionCuentaRegresiva(estado.inicioJornadas, estado.finJornadas)
        SeccionInscripcion(estado.inscripcion, acciones)
        SeccionProximasCharlas(estado.proximasCharlas, acciones)
        SeccionNovedades(estado.novedades, acciones)
        AccesosAsistente(acciones)
        EspacioFinal()
    }
}

@Composable
private fun AccesosAsistente(acciones: AccionesAsistente) {
    TituloSeccion("Accesos rápidos")
    FilaDeTarjetas {
        // Modifier.weight(1f) solo existe dentro de un Row/Column: por eso
        // FilaDeTarjetas recibe un lambda con RowScope
        AccesoRapido(R.drawable.outline_event_available_24, "Cronograma", acciones.abrirCronograma, Modifier.weight(1f))
        AccesoRapido(R.drawable.outline_check_circle_24, "Inscripción", acciones.abrirInscripcion, Modifier.weight(1f))
    }
    EspacioEntreFilas()
    FilaDeTarjetas {
        AccesoRapido(R.drawable.outline_add_location_alt_24, "Mapa de descuentos", acciones.abrirMapa, Modifier.weight(1f))
        AccesoRapido(R.drawable.baseline_help_outline_24, "Preguntas frecuentes", acciones.abrirFaq, Modifier.weight(1f))
    }
}

/* ------------------------------------------------------------------------
 * Previews: Android Studio las dibuja en el panel "Design"/"Split" de este
 * archivo. Usan datos de ejemplo, no Firebase.
 * ---------------------------------------------------------------------- */

internal val estadoAsistenteEjemplo = HomeAsistenteViewModel.Estado(
    cargando = false,
    inicioJornadas = LocalDate.now().plusDays(12),
    finJornadas = LocalDate.now().plusDays(14),
    inscripcion = SituacionInscripcion.FaltaPago(25000.0),
    proximasCharlas = listOf(
        CharlaProxima(
            "c1", "Variación craneofacial en poblaciones del NOA",
            LocalDate.now().plusDays(12), LocalTime.of(9, 0), LocalTime.of(9, 30), "Aula Magna"
        ),
        CharlaProxima(
            "c2", "Isótopos estables y dieta en el Holoceno tardío",
            LocalDate.now().plusDays(12), LocalTime.of(10, 30), LocalTime.of(11, 0), "Aula 3"
        )
    ),
    novedades = listOf(
        NovedadResumen("n1", "Se extendió el plazo de inscripción", LocalDate.now().minusDays(1)),
        NovedadResumen("n2", "Ya está disponible el cronograma", LocalDate.now().minusDays(5))
    )
)

@Preview(name = "Home asistente", showBackground = true, heightDp = 1400)
@Composable
private fun PreviewHomeAsistente() {
    TemaJnab {
        HomeAsistenteScreen("Lucía", estadoAsistenteEjemplo, AccionesAsistente())
    }
}

@Preview(name = "Home asistente vacío", showBackground = true, heightDp = 1100)
@Composable
private fun PreviewHomeAsistenteVacio() {
    TemaJnab {
        HomeAsistenteScreen(
            "Lucía",
            HomeAsistenteViewModel.Estado(cargando = false),
            AccionesAsistente()
        )
    }
}
