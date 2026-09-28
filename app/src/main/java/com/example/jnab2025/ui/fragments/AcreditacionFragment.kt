package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.R
import com.example.jnab2025.ui.compose.AcreditacionScreen
import com.example.jnab2025.ui.compose.TemaJnab
import com.example.jnab2025.ui.viewmodels.AcreditacionViewModel
import com.example.jnab2025.ui.viewmodels.AcreditacionViewModel.Evento
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.launch

/**
 * Acreditacion en la entrada (solo organizador, desde el drawer).
 *
 * El escaner es el de Google Play Services (Google Code Scanner): abre su
 * propia pantalla de camara, lee un QR y devuelve el texto. No hace falta
 * pedir el permiso de camara porque la camara la usa Play Services, no la app.
 */
class AcreditacionFragment : Fragment() {

    private val viewModel: AcreditacionViewModel by viewModels()

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
                val estado by viewModel.estado.collectAsStateWithLifecycle()

                TemaJnab {
                    AcreditacionScreen(
                        estado = estado,
                        onEscanear = ::escanear,
                        onElegir = viewModel::elegirPorBusqueda,
                        onAcreditar = viewModel::acreditar,
                        onVerInscriptos = {
                            findNavController().navigate(R.id.verInscriptosFragment)
                        },
                        onCerrar = viewModel::cerrarResultado
                    )
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        descargarEscanerSiFalta()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.eventos.collect { evento ->
                    when (evento) {
                        is Evento.Aviso -> mostrar(evento.mensaje, Toast.LENGTH_LONG)
                        is Evento.Acreditado -> {
                            mostrar("Ingreso registrado: ${evento.nombre}", Toast.LENGTH_SHORT)
                            if (evento.reabrirEscaner) escanear()
                        }
                    }
                }
            }
        }
    }

    private fun escanear() {
        val opciones = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        // si el organizador cierra la camara sin leer nada, la tarea se cancela
        // y no hay nada que hacer
        GmsBarcodeScanning.getClient(requireContext(), opciones)
            .startScan()
            .addOnSuccessListener { codigo ->
                viewModel.elegirPorQr(codigo.rawValue)
            }
            .addOnFailureListener { error ->
                mostrar("No se pudo abrir el escáner: ${error.message}", Toast.LENGTH_LONG)
            }
    }

    /*
     * La pantalla del escaner es un modulo de Play Services que se descarga la
     * primera vez que se usa. Se pide al entrar aca para que ya este bajado
     * cuando empiece la fila (el lugar del evento puede no tener buena senal).
     */
    private fun descargarEscanerSiFalta() {
        val contexto = requireContext().applicationContext
        val escaner = GmsBarcodeScanning.getClient(contexto)
        val instalador = ModuleInstall.getClient(contexto)

        instalador.areModulesAvailable(escaner)
            .addOnSuccessListener { respuesta ->
                if (!respuesta.areModulesAvailable()) {
                    instalador.installModules(
                        ModuleInstallRequest.newBuilder().addApi(escaner).build()
                    )
                }
            }
    }

    private fun mostrar(mensaje: String, duracion: Int) {
        val contexto = context ?: return
        Toast.makeText(contexto, mensaje, duracion).show()
    }
}
