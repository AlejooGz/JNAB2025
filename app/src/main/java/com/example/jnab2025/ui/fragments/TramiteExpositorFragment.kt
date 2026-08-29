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
import androidx.navigation.fragment.navArgs
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentTramiteExpositorBinding
import com.example.jnab2025.ui.viewmodels.EnviarTrabajoViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TramiteExpositorFragment : Fragment() {
    private var _binding: FragmentTramiteExpositorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EnviarTrabajoViewModel by viewModels()
    private val args: TramiteExpositorFragmentArgs by navArgs()
    private var archivoUri: Uri? = null
    private var nombreArchivo: String? = null
    private val elegirPdf = registerForActivityResult(
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTramiteExpositorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnSeleccionarArchivo.setOnClickListener {
            elegirPdf.launch(arrayOf("application/pdf"))
        }
        binding.btnEnviarTramite.setOnClickListener {
            viewModel.enviar(
                simposioId = args.simposioId,
                titulo = binding.etTituloTrabajo.text.toString(),
                resumen = binding.etResumenTrabajo.text.toString(),
                archivoUri = archivoUri,
                nombreArchivo = nombreArchivo
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.envios.collectLatest { envio ->
                    when (envio) {
                        EnviarTrabajoViewModel.Envio.Ok -> {
                            avisar("Trabajo enviado")
                            findNavController().navigate(
                                R.id.action_tramiteExpositorFragment_to_seguimientoTramiteFragment2
                            )
                        }
                        is EnviarTrabajoViewModel.Envio.Error -> avisar(envio.mensaje)
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
        } ?: "trabajo.pdf"
    }
    private fun avisar(mensaje: String) {
        Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
