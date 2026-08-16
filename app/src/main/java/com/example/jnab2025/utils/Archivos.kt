package com.example.jnab2025.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Abre los PDF adjuntos a los trabajos con el visor que tenga el dispositivo. */
object Archivos {

    sealed interface Resultado {
        data object Abierto : Resultado
        data object SinArchivo : Resultado
        data object NoDisponible : Resultado
        data object SinVisor : Resultado
    }

    fun abrirPdf(context: Context, uriTexto: String?): Resultado {
        if (uriTexto.isNullOrBlank()) return Resultado.SinArchivo

        val uri = runCatching { Uri.parse(uriTexto) }.getOrNull() ?: return Resultado.SinArchivo

        // Se chequea el acceso antes de lanzar el visor para poder dar un mensaje
        // claro. Los trabajos del seed traen un URI de ejemplo que no apunta a
        // ningun archivo real, y sin esto el visor abriria en blanco.
        val accesible = runCatching {
            context.contentResolver.openInputStream(uri)?.use { true } ?: false
        }.getOrDefault(false)

        if (!accesible) return Resultado.NoDisponible

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return try {
            context.startActivity(intent)
            Resultado.Abierto
        } catch (e: ActivityNotFoundException) {
            Resultado.SinVisor
        }
    }

    /** El mensaje a mostrar, o null si se abrio bien. */
    fun mensajeDe(resultado: Resultado): String? = when (resultado) {
        Resultado.Abierto -> null
        Resultado.SinArchivo -> "Este trabajo no tiene PDF adjunto"
        Resultado.NoDisponible ->
            "El PDF no esta disponible. Los trabajos de ejemplo no tienen archivo real."
        Resultado.SinVisor -> "No hay ninguna app instalada para abrir PDF"
    }
}
