package com.example.jnab2025.ui.fragments

import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.NavGraphDirections
import com.example.jnab2025.R
import com.example.jnab2025.ui.compose.AccionesAsistente
import com.example.jnab2025.utils.Sesion
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/*
 * Lo que comparten los homes hechos en Compose (asistente y expositor):
 * a donde lleva cada toque, el saludo y los avisos de error.
 */

/** Primer nombre del usuario logueado, para el "¡Hola, ...!". */
internal fun Fragment.primerNombre(porDefecto: String): String =
    Sesion.nombre(requireContext())
        .trim()
        .split(Regex("\\s+"))
        .firstOrNull()
        .orEmpty()
        .ifBlank { porDefecto }

/**
 * Cambia de pestaña en la barra de abajo. Para Charlas, Novedades, Mapa y FAQ
 * se hace asi en lugar de navigate(): de esa forma la pestaña queda marcada,
 * igual que si el usuario la hubiera tocado.
 */
internal fun Fragment.irAPestania(@IdRes destino: Int) {
    requireActivity()
        .findViewById<BottomNavigationView>(R.id.bottom_navigation)
        ?.selectedItemId = destino
}

/** Traduce cada toque de las secciones del asistente a una navegacion. */
internal fun Fragment.accionesAsistente() = AccionesAsistente(
    abrirInscripcion = { findNavController().navigate(R.id.inscripcionFragment) },
    abrirComprobante = { findNavController().navigate(R.id.cargarComprobanteFragment) },
    abrirCharla = { charlaId ->
        // accion global del nav_graph: se puede usar desde cualquier pantalla
        findNavController().navigate(
            NavGraphDirections.actionAgendaFragmentToCharlaDetailFragment(charlaId)
        )
    },
    abrirCronograma = { irAPestania(R.id.agendaFragment) },
    abrirNovedades = { irAPestania(R.id.novedadesFragment) },
    abrirMapa = { irAPestania(R.id.mapsFragment) },
    abrirFaq = { irAPestania(R.id.faqFragment) }
)

/** Muestra como Toast los errores que emite un ViewModel del home. */
internal fun Fragment.mostrarAvisos(avisos: Flow<String>) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            avisos.collect { mensaje ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
            }
        }
    }
}
