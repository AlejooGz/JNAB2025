package com.example.jnab2025.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel.CharlaProxima
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
    credencial: DatosCredencial,
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
            subtitulo = "Tu paso por las Jornadas",
            imagen = IMAGEN_ENCABEZADO_ASISTENTE,
            // el pinguino esta a la derecha de la imagen
            alineacion = Alignment.CenterEnd
        )

        // mientras llegan los primeros datos, solo la ruedita
        if (estado.cargando) {
            IndicadorCarga()
            return@Column
        }

        SeccionCuentaRegresiva(estado.inicioJornadas, estado.finJornadas)
        SeccionCredencial(credencial, estado.inscripcion, acciones)
        SeccionProximasCharlas(estado.proximasCharlas, acciones)
        EspacioFinal()
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
    )
)

private val credencialAsistenteEjemplo =
    DatosCredencial("Lucía Fernández", "Asistente", "uid-de-ejemplo", AVATAR_ASISTENTE)

@Preview(name = "Home asistente", showBackground = true, heightDp = 1400)
@Composable
private fun PreviewHomeAsistente() {
    TemaJnab {
        HomeAsistenteScreen("Lucía", credencialAsistenteEjemplo, estadoAsistenteEjemplo, AccionesAsistente())
    }
}

@Preview(name = "Home asistente vacío", showBackground = true, heightDp = 1100)
@Composable
private fun PreviewHomeAsistenteVacio() {
    TemaJnab {
        HomeAsistenteScreen(
            "Lucía",
            credencialAsistenteEjemplo,
            HomeAsistenteViewModel.Estado(cargando = false),
            AccionesAsistente()
        )
    }
}
