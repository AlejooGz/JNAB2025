package com.example.jnab2025.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.jnab2025.R
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel.SituacionInscripcion
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import kotlin.math.PI
import kotlin.math.sin

/*
 * Credencial de "Mi inscripcion": una tarjetita celeste con olas, el avatar
 * del animal del rol, el sello del estado de la inscripcion y un QR con el
 * UID del usuario. La usan los homes del expositor y del asistente.
 */

/*
 * Animal de cada rol. Las imagenes estan en drawable-nodpi: ahi Android no
 * las agranda segun la densidad de pantalla al decodificarlas (en drawable/
 * un 1774x887 ocupaba ~50 MB de RAM en un xxhdpi).
 */

/** Lobo marino, el animal del expositor. */
@DrawableRes
internal val IMAGEN_ENCABEZADO_EXPOSITOR: Int = R.drawable.lobo_marino_encabezado

@DrawableRes
internal val AVATAR_EXPOSITOR: Int = R.drawable.lobo_marino_avatar

/** Pinguino, el animal del asistente. */
@DrawableRes
internal val IMAGEN_ENCABEZADO_ASISTENTE: Int = R.drawable.pinguino_encabezado

@DrawableRes
internal val AVATAR_ASISTENTE: Int = R.drawable.pinguino_avatar

/** Lo que la credencial muestra de la persona. [uid] va dentro del QR. */
data class DatosCredencial(
    val nombre: String,
    val rol: String,
    val uid: String?,
    @DrawableRes val avatar: Int
)

// Paleta propia de la tarjeta (el "mar" de la credencial)
private val CelesteClaro = Color(0xFFE3F3F6)
private val CelesteMedio = Color(0xFFBFE2EA)
private val AguaOla = Color(0xFF7CC3D3)
private val AzulTinta = Color(0xFF1F3A68)
private val AzulPetroleo = Color(0xFF0E5A6B)
private val Ambar = Color(0xFFB07800)

/**
 * Como se ve el sello segun la situacion de la inscripcion. Solo con la
 * inscripcion acreditada se puede abrir la credencial; en los demas casos
 * el boton es la accion que destraba la situacion (si la hay).
 */
private data class Sello(
    val texto: String,
    val color: Color,
    val detalle: String?,
    val acreditado: Boolean,
    val boton: String?,
    val onBoton: () -> Unit
)

@Composable
private fun selloDe(
    situacion: SituacionInscripcion,
    acciones: AccionesAsistente,
    abrirCredencial: () -> Unit
): Sello {
    val verde = MaterialTheme.colorScheme.tertiary
    val rojo = MaterialTheme.colorScheme.primary

    return when (situacion) {
        SituacionInscripcion.Confirmada -> Sello(
            "ACREDITADO", verde, null, true,
            "Ver credencial", abrirCredencial
        )
        SituacionInscripcion.SinInscripcion -> Sello(
            "SIN INSCRIPCIÓN", Ambar, "Todavía no estás inscripto a las Jornadas.", false,
            "Inscribirme", acciones.abrirInscripcion
        )
        is SituacionInscripcion.FaltaPago -> Sello(
            "PAGO PENDIENTE", Ambar, "Falta acreditar el pago de $${situacion.monto.toInt()}.", false,
            "Cargar comprobante", acciones.abrirComprobante
        )
        SituacionInscripcion.ComprobanteEnRevision -> Sello(
            "EN REVISIÓN", Ambar, "La organización está verificando tu comprobante.", false,
            null, {}
        )
        is SituacionInscripcion.ComprobanteRechazado -> Sello(
            "COMPROBANTE RECHAZADO", rojo,
            situacion.motivo?.let { "Motivo: $it" } ?: "Cargá un comprobante nuevo.", false,
            "Cargar otro comprobante", acciones.abrirComprobante
        )
        SituacionInscripcion.Anulada -> Sello(
            "ANULADA", rojo, "Contactate con la organización.", false,
            null, {}
        )
    }
}

/** Seccion "Mi inscripcion" con la credencial en lugar de la tarjeta de texto. */
@Composable
fun SeccionCredencial(
    datos: DatosCredencial,
    situacion: SituacionInscripcion,
    acciones: AccionesAsistente
) {
    TituloSeccion("Mi inscripción")

    // rememberSaveable: el dialogo sigue abierto si se rota la pantalla
    var mostrarCredencial by rememberSaveable { mutableStateOf(false) }
    val sello = selloDe(situacion, acciones) { mostrarCredencial = true }

    TarjetaCredencial(datos, sello, onClick = acciones.abrirInscripcion)

    if (mostrarCredencial && sello.acreditado) {
        DialogoCredencial(datos, sello) { mostrarCredencial = false }
    }
}

@Composable
private fun TarjetaCredencial(
    datos: DatosCredencial,
    sello: Sello,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CelesteClaro),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fondoMarino()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(datos.avatar, 88.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = datos.nombre,
                        color = AzulTinta,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    ChipRol(datos.rol, Modifier.padding(top = 6.dp))
                    FilaSello(sello, Modifier.padding(top = 6.dp))
                    if (sello.detalle != null) {
                        Text(
                            text = sello.detalle,
                            color = AzulTinta,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // sin acreditar el QR no sirve en la entrada: se muestra atenuado
                CajaQr(
                    uid = datos.uid,
                    lado = 64.dp,
                    modifier = Modifier.alpha(if (sello.acreditado) 1f else 0.3f)
                )
            }

            if (sello.boton != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    BotonCredencial(sello.boton, sello.onBoton)
                }
            }
        }
    }
}

/** La credencial ampliada, con el QR grande para mostrar en la acreditacion. */
@Composable
private fun DialogoCredencial(
    datos: DatosCredencial,
    sello: Sello,
    onCerrar: () -> Unit
) {
    Dialog(onDismissRequest = onCerrar) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CelesteClaro),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .fondoMarino()
                    .padding(20.dp)
            ) {
                Text(
                    text = "JNAB 2025",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("Credencial", color = AzulTinta, fontSize = 14.sp)

                Avatar(datos.avatar, 96.dp, Modifier.padding(top = 14.dp))
                Text(
                    text = datos.nombre,
                    color = AzulTinta,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )
                ChipRol(datos.rol, Modifier.padding(top = 6.dp))
                FilaSello(sello, Modifier.padding(top = 6.dp))

                CajaQr(
                    uid = datos.uid,
                    lado = 200.dp,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = "Mostrá este código en la acreditación",
                    color = AzulTinta,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )

                TextButton(onClick = onCerrar, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Cerrar", color = AzulPetroleo, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* ------------------------------ piezas chicas ------------------------------ */

@Composable
private fun Avatar(@DrawableRes imagen: Int, tam: Dp, modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(imagen),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(tam)
            .border(4.dp, Color.White, CircleShape)
            .padding(4.dp)
            .clip(CircleShape)
    )
}

@Composable
private fun ChipRol(rol: String, modifier: Modifier = Modifier) {
    Text(
        text = rol.uppercase(),
        color = MaterialTheme.colorScheme.onPrimary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 3.dp)
    )
}

@Composable
private fun FilaSello(sello: Sello, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        if (sello.acreditado) {
            Icon(
                painter = painterResource(R.drawable.outline_check_circle_24),
                contentDescription = null,
                tint = sello.color,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(sello.color, CircleShape)
            )
        }
        Text(
            text = sello.texto,
            color = sello.color,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}

@Composable
private fun BotonCredencial(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.5.dp, Color.White),
        colors = ButtonDefaults.buttonColors(
            containerColor = AzulPetroleo,
            contentColor = Color.White
        )
    ) {
        Text(texto, fontSize = 14.sp)
        Icon(
            painter = painterResource(R.drawable.outline_chevron_right_24),
            contentDescription = null,
            modifier = Modifier
                .padding(start = 2.dp)
                .size(20.dp)
        )
    }
}

/** QR sobre fondo blanco redondeado, como en el diseno. */
@Composable
private fun CajaQr(uid: String?, lado: Dp, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        if (uid.isNullOrBlank()) {
            // sin sesion de Firebase no hay nada que codificar
            Box(modifier = Modifier.size(lado))
        } else {
            CodigoQr(uid, Modifier.size(lado))
        }
    }
}

/**
 * Dibuja el QR modulo por modulo en un Canvas: no hace falta armar un
 * Bitmap y queda nitido a cualquier tamano. El Encoder de ZXing devuelve la
 * matriz a razon de un casillero por modulo.
 */
@Composable
fun CodigoQr(contenido: String, modifier: Modifier = Modifier) {
    val matriz: ByteMatrix? = remember(contenido) {
        runCatching { Encoder.encode(contenido, ErrorCorrectionLevel.M).matrix }.getOrNull()
    }

    Canvas(modifier = modifier) {
        val m = matriz ?: return@Canvas
        val modulo = size.minDimension / m.width
        // un pelito de solapamiento para que no queden lineas entre modulos
        val tam = Size(modulo + 0.6f, modulo + 0.6f)
        for (y in 0 until m.height) {
            for (x in 0 until m.width) {
                if (m.get(x, y).toInt() == 1) {
                    drawRect(Color.Black, topLeft = Offset(x * modulo, y * modulo), size = tam)
                }
            }
        }
    }
}

/**
 * Fondo celeste de la credencial: degrade, dos capas de olas abajo y unas
 * burbujas. Se dibuja detras del contenido, asi se adapta a cualquier ancho.
 */
private fun Modifier.fondoMarino(): Modifier = this
    .background(Brush.verticalGradient(listOf(CelesteClaro, CelesteMedio)))
    .drawBehind {
        dibujarOla(alturaRelativa = 0.70f, amplitud = 7.dp.toPx(), ondas = 1.6f, color = AguaOla.copy(alpha = 0.30f))
        dibujarOla(alturaRelativa = 0.82f, amplitud = 5.dp.toPx(), ondas = 2.3f, color = AguaOla.copy(alpha = 0.45f))
        dibujarBurbujas()
    }

private fun DrawScope.dibujarOla(
    alturaRelativa: Float,
    amplitud: Float,
    ondas: Float,
    color: Color
) {
    val base = size.height * alturaRelativa
    val camino = Path().apply {
        moveTo(0f, base)
        val pasos = 40
        for (i in 1..pasos) {
            val x = size.width * i / pasos
            val y = base + amplitud * sin(2 * PI * ondas * i / pasos).toFloat()
            lineTo(x, y)
        }
        lineTo(size.width, size.height)
        lineTo(0f, size.height)
        close()
    }
    drawPath(camino, color)
}

/** Posiciones relativas (x, y, radio en dp) de las burbujas decorativas. */
private val burbujas = listOf(
    Triple(0.03f, 0.18f, 5f),
    Triple(0.06f, 0.34f, 3f),
    Triple(0.04f, 0.78f, 6f),
    Triple(0.72f, 0.62f, 4f),
    Triple(0.95f, 0.10f, 3f)
)

private fun DrawScope.dibujarBurbujas() {
    burbujas.forEach { (x, y, radio) ->
        val centro = Offset(size.width * x, size.height * y)
        drawCircle(AguaOla.copy(alpha = 0.35f), radius = radio.dp.toPx(), center = centro)
        drawCircle(
            Color.White.copy(alpha = 0.7f),
            radius = radio.dp.toPx(),
            center = centro,
            style = Stroke(width = 1.dp.toPx())
        )
    }
}

/* --------------------------------- Previews --------------------------------- */

private val datosEjemplo = DatosCredencial(
    nombre = "Alejo Gonzalez",
    rol = "Expositor",
    uid = "uid-de-ejemplo-123",
    avatar = AVATAR_EXPOSITOR
)

@Preview(name = "Credencial acreditada", showBackground = true)
@Composable
private fun PreviewCredencialAcreditada() {
    TemaJnab {
        Column {
            SeccionCredencial(datosEjemplo, SituacionInscripcion.Confirmada, AccionesAsistente())
        }
    }
}

@Preview(name = "Credencial con pago pendiente", showBackground = true)
@Composable
private fun PreviewCredencialPendiente() {
    TemaJnab {
        Column {
            SeccionCredencial(datosEjemplo, SituacionInscripcion.FaltaPago(25000.0), AccionesAsistente())
        }
    }
}
