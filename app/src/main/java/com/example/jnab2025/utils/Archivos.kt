package com.example.jnab2025.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/** Abre los PDF adjuntos a los trabajos. */
object Archivos {
    sealed interface Resultado {
        data object Abierto : Resultado
        data object SinArchivo : Resultado
        data object NoDisponible : Resultado
        data object SinVisor : Resultado
    }
    fun abrirPdf(
        context: Context,
        uriTexto: String?
    ): Resultado {
        if (uriTexto.isNullOrBlank()) {
            return Resultado.SinArchivo
        }
        val uri = runCatching { Uri.parse(uriTexto)
        }.getOrNull()
            ?: return Resultado.SinArchivo
        return when (uri.scheme?.lowercase()) {
            "http", "https" ->
                abrirPdfRemoto(context, uri)
            "content", "file" ->
                abrirPdfLocal(context, uri)
            else -> Resultado.NoDisponible
        }
    }

    /**
     * Abre un PDF remoto, por ejemplo una URL de Firebase Storage.
     */
    private fun abrirPdfRemoto(
        context: Context,
        uri: Uri
    ): Resultado {
        val intent = Intent(
            Intent.ACTION_VIEW, uri
        )
        return try {
            context.startActivity(intent)
            Resultado.Abierto
        } catch (e: ActivityNotFoundException) {
            Resultado.SinVisor
        }
    }

    /**
     * Abre un PDF local seleccionado desde el dispositivo.
     */
    private fun abrirPdfLocal(
        context: Context,
        uri: Uri
    ): Resultado {
        val accesible = runCatching {
            context.contentResolver
                .openInputStream(uri)
                ?.use { true }
                ?: false
        }.getOrDefault(false)
        if (!accesible) {
            return Resultado.NoDisponible
        }
        val intent = Intent(Intent.ACTION_VIEW
        ).apply {
            setDataAndType(uri, "application/pdf"
            )
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        return try {
            context.startActivity(intent)
            Resultado.Abierto
        } catch (e: ActivityNotFoundException) {
            Resultado.SinVisor
        }
    }
    /** El mensaje a mostrar, o null si se abrió correctamente. */
    fun mensajeDe(
        resultado: Resultado
    ): String? = when (resultado) {
        Resultado.Abierto -> null
        Resultado.SinArchivo -> "Este trabajo no tiene PDF adjunto"
        Resultado.NoDisponible -> "El PDF no está disponible"
        Resultado.SinVisor -> "No hay ninguna aplicación disponible para abrir el PDF"
    }
}