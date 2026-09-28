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
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.R
import com.example.jnab2025.ui.compose.AVATAR_EXPOSITOR
import com.example.jnab2025.ui.compose.AccionesExpositor
import com.example.jnab2025.ui.compose.DatosCredencial
import com.example.jnab2025.ui.compose.HomeExpositorScreen
import com.example.jnab2025.ui.compose.TemaJnab
import com.example.jnab2025.ui.viewmodels.HomeAsistenteViewModel
import com.example.jnab2025.ui.viewmodels.HomeExpositorViewModel
import com.example.jnab2025.utils.Sesion

/**
 * Home del expositor. Igual que [HomeAsistenteFragment], pero con dos
 * ViewModels: el propio (trabajos y presentaciones) y el del asistente, del
 * que salen las secciones compartidas.
 */
class HomeExpositorFragment : Fragment() {

    private val viewModel: HomeExpositorViewModel by viewModels()
    private val viewModelAsistente: HomeAsistenteViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val nombre = primerNombre("Expositor")
        val credencial = DatosCredencial(
            nombre = Sesion.nombre(requireContext()).trim().ifBlank { nombre },
            rol = "Expositor",
            uid = Sesion.firebaseUid(requireContext()),
            avatar = AVATAR_EXPOSITOR
        )
        val accionesAsistente = accionesAsistente()
        val acciones = AccionesExpositor(
            abrirMisTrabajos = { findNavController().navigate(R.id.seguimientoTramiteFragment) }
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
            )
            setContent {
                val estado by viewModel.estado.collectAsStateWithLifecycle()
                val estadoAsistente by viewModelAsistente.estado.collectAsStateWithLifecycle()

                TemaJnab {
                    HomeExpositorScreen(
                        nombre = nombre,
                        credencial = credencial,
                        estado = estado,
                        estadoAsistente = estadoAsistente,
                        acciones = acciones,
                        accionesAsistente = accionesAsistente
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrarAvisos(viewModel.avisos)
        mostrarAvisos(viewModelAsistente.avisos)
    }
}
