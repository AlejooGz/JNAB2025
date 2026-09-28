package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.jnab2025.ui.compose.AVATAR_ASISTENTE
import com.example.jnab2025.ui.compose.DatosCredencial
import com.example.jnab2025.ui.compose.HomeAsistenteScreen
import com.example.jnab2025.ui.compose.TemaJnab
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel
import com.example.jnab2025.utils.Sesion

/**
 * Puente entre el mundo de Fragments/XML y Compose.
 *
 * En vez de inflar un layout con view binding, onCreateView devuelve un
 * ComposeView: una View comun cuyo contenido se dibuja con funciones
 * @Composable. Asi el home entra en el mismo contenedor de MainFragment que
 * el del organizador, sin tocar la navegacion.
 */
class HomeAsistenteFragment : Fragment() {

    private val viewModel: HomeAsistenteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val nombre = primerNombre("Asistente")
        val credencial = DatosCredencial(
            nombre = Sesion.nombre(requireContext()).trim().ifBlank { nombre },
            rol = "Asistente",
            uid = Sesion.firebaseUid(requireContext()),
            avatar = AVATAR_ASISTENTE
        )
        val acciones = accionesAsistente()

        return ComposeView(requireContext()).apply {
            // libera la composicion cuando se destruye la vista del Fragment
            // (no cuando se desadjunta), que es el ciclo de vida correcto aca
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                // Convierte el StateFlow en estado de Compose: cada valor nuevo
                // que emite el ViewModel redibuja la pantalla. "WithLifecycle"
                // deja de escuchar cuando la app pasa a segundo plano.
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                TemaJnab {
                    HomeAsistenteScreen(
                        nombre = nombre,
                        credencial = credencial,
                        estado = estado,
                        acciones = acciones
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrarAvisos(viewModel.avisos)
    }
}
