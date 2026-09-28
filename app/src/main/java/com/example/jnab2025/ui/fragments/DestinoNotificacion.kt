package com.example.jnab2025.ui.fragments

import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.jnab2025.NavGraphDirections
import com.example.jnab2025.R
import com.example.jnab2025.data.model.TipoNotificacion

/**
 * A que pantalla lleva cada tipo de aviso. Lo usan la pantalla de
 * notificaciones (al tocar un renglon) y MainActivity (al tocar el aviso en la
 * bandeja del telefono), asi los dos caminos llevan al mismo lugar.
 *
 * Devuelve false si ese tipo no tiene una pantalla propia.
 */
internal fun NavController.abrirDestinoDeAviso(
    tipo: String,
    referenciaId: String,
    opciones: NavOptions? = null
): Boolean {
    when (tipo) {
        // los dos apuntan a una charla: el detalle muestra dia, horario y aula
        TipoNotificacion.RECORDATORIO_CHARLA.name,
        TipoNotificacion.PRESENTACION_PROGRAMADA.name -> {
            if (referenciaId.isBlank()) return false
            // accion global del nav_graph, igual que desde el home
            navigate(
                NavGraphDirections.actionAgendaFragmentToCharlaDetailFragment(referenciaId),
                opciones
            )
        }

        TipoNotificacion.PAGO_APROBADO.name ->
            navigate(R.id.inscripcionFragment, null, opciones)

        // el organizador va a la lista de inscriptos, donde verifica comprobantes
        TipoNotificacion.COMPROBANTE_RECIBIDO.name ->
            navigate(R.id.verInscriptosFragment, null, opciones)

        // el mapa se centra en el lugar y abre su detalle
        TipoNotificacion.LUGAR_AGREGADO.name ->
            navigate(
                R.id.mapsFragment,
                bundleOf(MapsFragment.ARG_LUGAR_ID to referenciaId.ifBlank { null }),
                opciones
            )

        // el organizador va a las propuestas del simposio que recibio el trabajo
        TipoNotificacion.TRABAJO_ENVIADO.name ->
            if (referenciaId.isBlank()) {
                navigate(R.id.misSimposiosFragment, null, opciones)
            } else {
                navigate(
                    R.id.propuestasFragment,
                    bundleOf("simposioId" to referenciaId),
                    opciones
                )
            }

        // el expositor ve el estado de sus trabajos
        TipoNotificacion.TRABAJO_ACEPTADO.name,
        TipoNotificacion.TRABAJO_RECHAZADO.name ->
            navigate(R.id.seguimientoTramiteFragment, null, opciones)

        else -> return false
    }
    return true
}
