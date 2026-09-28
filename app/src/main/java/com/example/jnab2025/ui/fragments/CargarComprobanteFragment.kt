package com.example.jnab2025.ui.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.databinding.FragmentCargarComprobanteBinding
import com.example.jnab2025.ui.viewmodels.InscripcionViewModel
import com.example.jnab2025.utils.mostrarCargando
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Carga del comprobante de pago. Ya no recibe argumentos: el comprobante cuelga
 * de la inscripcion del usuario logueado, no de un trabajo. Eso elimina de raiz
 * el bug de la clave de bundle que hacia fallar la navegacion.
 */
class CargarComprobanteFragment : Fragment() {

    private var _binding: FragmentCargarComprobanteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: InscripcionViewModel by viewModels()

    private var archivoUri: Uri? = null
    private var nombreArchivo: String? = null

    private val elegirArchivo = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@registerForActivityResult

        runCatching {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        archivoUri = uri
        nombreArchivo = nombreDe(uri)
        binding.tvArchivoSeleccionado.text = "Archivo: $nombreArchivo"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCargarComprobanteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSeleccionarArchivo.setOnClickListener {
            elegirArchivo.launch(arrayOf("application/pdf", "image/*"))
        }

        binding.btnEnviarComprobante.setOnClickListener {
            viewModel.cargarComprobante(
                archivoUri,
                nombreArchivo
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.vista.collectLatest { vista ->
                        val inscripcion = vista.inscripcion
                        binding.tvDetalle.text = if (inscripcion == null) {
                            "Primero tenés que inscribirte al evento."
                        } else {
                            val tipo = inscripcion.tipo.lowercase()
                            val categoria = inscripcion.categoria.lowercase()

                            "Inscripción de $tipo · $categoria " +
                                "por $${inscripcion.monto.toInt()}"
                        }
                        binding.btnEnviarComprobante.isEnabled = inscripcion != null
                    }
                }
                launch {
                    viewModel.enviando.collectLatest { mostrarCargando(it) }
                }
                launch {
                    viewModel.avisos.collectLatest { aviso ->
                        Toast.makeText(requireContext(), aviso, Toast.LENGTH_LONG).show()
                        if (aviso.startsWith("Comprobante enviado")) {
                            findNavController().popBackStack()
                        }
                    }
                }
            }
        }
    }

    private fun nombreDe(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val indice = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (indice >= 0 && it.moveToFirst()) it.getString(indice) else null
        } ?: "comprobante"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mostrarCargando(false)
        _binding = null
    }
}
