package com.example.jnab2025.ui.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.jnab2025.R

/**
 * Tema de las pantallas hechas en Compose.
 *
 * Los colores se leen de res/values/colors.xml (los mismos que usan los XML),
 * asi la paleta sigue definida en un solo lugar. Adentro de TemaJnab { ... }
 * cualquier composable los obtiene con MaterialTheme.colorScheme.
 *
 *   primary    -> dark_red   (titulos, numeros, iconos)
 *   background -> mint_green (fondo de pantalla)
 *   surface    -> cream      (tarjetas)
 *   tertiary   -> green      (estados "ok")
 */
@Composable
fun TemaJnab(content: @Composable () -> Unit) {
    val colores = lightColorScheme(
        primary = colorResource(R.color.dark_red),
        onPrimary = Color.White,
        background = colorResource(R.color.mint_green),
        onBackground = Color.Black,
        surface = colorResource(R.color.cream),
        onSurface = Color.Black,
        tertiary = colorResource(R.color.green),
        onTertiary = Color.White
    )
    MaterialTheme(
        colorScheme = colores,
        content = content
    )
}
