package com.example.jnab2025.ui.compose

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jnab2025.R

/*
 * Piezas reutilizables de los homes. Cada funcion @Composable es un pedazo de
 * UI: recibe datos por parametro y los dibuja. No guardan estado ni hablan con
 * Firebase; eso lo resuelve el ViewModel y les llega ya masticado.
 */

/** Encabezado con la imagen tematica y el saludo, igual al del organizador. */
@Composable
fun EncabezadoHome(
    saludo: String,
    subtitulo: String
) {
    // Box apila a sus hijos: primero la imagen de fondo, encima los textos
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.fondoo),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier.padding(start = 20.dp, top = 24.dp, end = 20.dp)
        ) {
            Text(
                text = saludo,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitulo,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 21.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 22.dp, bottom = 10.dp)
    )
}

/**
 * La tarjeta crema redondeada que se usa en todos los homes. Si recibe
 * onClick es tocable. El contenido va como lambda: en Compose un
 * "contenedor" es una funcion que recibe otra funcion para dibujar adentro.
 */
@Composable
fun TarjetaJnab(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val forma = RoundedCornerShape(14.dp)
    val colores = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    val elevacion = CardDefaults.cardElevation(defaultElevation = 4.dp)

    if (onClick != null) {
        Card(onClick = onClick, modifier = modifier, shape = forma, colors = colores, elevation = elevacion) {
            Column(modifier = Modifier.padding(14.dp), content = contenido)
        }
    } else {
        Card(modifier = modifier, shape = forma, colors = colores, elevation = elevacion) {
            Column(modifier = Modifier.padding(14.dp), content = contenido)
        }
    }
}

/** Tarjeta de numero grande + etiqueta, como las del resumen del organizador. */
@Composable
fun TarjetaContador(
    cantidad: Int,
    etiqueta: String,
    accion: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaJnab(modifier = modifier.height(130.dp), onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = cantidad.toString(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 31.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = etiqueta,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(top = 6.dp)
            )
            Text(
                text = accion,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

/** Boton cuadrado de "Accesos rapidos". */
@Composable
fun AccesoRapido(
    @DrawableRes icono: Int,
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TarjetaJnab(modifier = modifier.height(100.dp), onClick = onClick) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icono),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = texto,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

/**
 * Fila que reparte el ancho en partes iguales entre sus hijos
 * (el equivalente a layout_weight="1" de los XML).
 */
@Composable
fun FilaDeTarjetas(contenido: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        content = contenido
    )
}

/** Renglon de una lista dentro de una tarjeta: titulo en negrita y detalle. */
@Composable
fun RenglonLista(
    titulo: String,
    detalle: String,
    modifier: Modifier = Modifier,
    extra: String? = null
) {
    Column(modifier = modifier.padding(vertical = 6.dp)) {
        Text(
            text = titulo,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = detalle,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp
        )
        if (extra != null) {
            Text(text = extra, fontSize = 13.sp)
        }
    }
}

/** Enlace de texto al pie de una tarjeta ("Ver todas", "Ir al cronograma"). */
@Composable
fun EnlaceTarjeta(texto: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = onClick) {
            Text(text = texto, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun TextoVacio(texto: String) {
    Text(text = texto, fontSize = 14.sp, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
fun IndicadorCarga() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun EspacioEntreFilas() {
    Spacer(modifier = Modifier.height(10.dp))
}

@Composable
fun EspacioFinal() {
    Spacer(modifier = Modifier.height(24.dp))
}
