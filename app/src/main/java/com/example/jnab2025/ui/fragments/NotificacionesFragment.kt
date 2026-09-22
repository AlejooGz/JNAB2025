package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.NavGraphDirections
import com.example.jnab2025.R
import com.example.jnab2025.data.model.NotificacionFirebase
import com.example.jnab2025.data.model.TipoNotificacion
import com.example.jnab2025.ui.compose.NotificacionesScreen
import com.example.jnab2025.ui.compose.TemaJnab
import com.example.jnab2025.ui.viewmodels.NotificacionesViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Pantalla a la que lleva la campanita del home (expositor y asistente).
 *
 * Usa el NotificacionesViewModel de la Activity, que ya tiene el listener de
 * Firestore abierto: no hace falta una segunda consulta.
 */
class NotificacionesFragment : Fragment() {

    private val viewModel: NotificacionesViewModel by activityViewModels()

    /** Las que llegaron sin leer mientras la pantalla estuvo abierta. */
    private val resaltadas = MutableStateFlow<Set<String>>(emptySet())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val notificaciones by viewModel.notificaciones.collectAsStateWithLifecycle()
                val nuevas by resaltadas.collectAsStateWithLifecycle()

                TemaJnab {
                    NotificacionesScreen(
                        notificaciones = notificaciones,
                        resaltadas = nuevas,
                        onAbrir = ::abrir
                    )
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Estar en esta pantalla equivale a haberlas visto: todo lo que llega
        // sin leer se recuerda como "nueva" para destacarlo y se marca leida,
        // con lo que el contador de la campanita vuelve a cero. Incluye las que
        // lleguen mientras el usuario sigue aca.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notificaciones.collect { lista ->
                    val sinLeer = lista.orEmpty().filter { !it.leida }.map { it.id }
                    if (sinLeer.isNotEmpty()) {
                        resaltadas.update { it + sinLeer }
                        viewModel.marcarTodasLeidas()
                    }
                }
            }
        }
    }

    /** Lleva a la pantalla relacionada con el aviso, si tiene una. */
    private fun abrir(notificacion: NotificacionFirebase) {
        when (notificacion.tipo) {
            TipoNotificacion.RECORDATORIO_CHARLA.name -> {
                if (notificacion.referenciaId.isBlank()) return
                // accion global del nav_graph, igual que desde el home
                findNavController().navigate(
                    NavGraphDirections.actionAgendaFragmentToCharlaDetailFragment(
                        notificacion.referenciaId
                    )
                )
            }

            TipoNotificacion.PAGO_APROBADO.name ->
                findNavController().navigate(R.id.inscripcionFragment)

            // el mapa se centra en el lugar y abre su detalle
            TipoNotificacion.LUGAR_AGREGADO.name ->
                findNavController().navigate(
                    R.id.mapsFragment,
                    bundleOf(
                        MapsFragment.ARG_LUGAR_ID to
                                notificacion.referenciaId.ifBlank { null }
                    )
                )
        }
    }
}