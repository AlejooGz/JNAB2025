package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.databinding.FragmentCargarComprobanteBinding
import com.example.jnab2025.data.TramiteRepository

class CargarComprobanteFragment : Fragment() {

    private var _binding: FragmentCargarComprobanteBinding? = null
    private val binding get() = _binding!!

    private var archivoUri: Uri? = null
    private var nombreArchivo = ""

    private val seleccionarArchivoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            archivoUri = result.data?.data
            archivoUri?.let { uri ->
                nombreArchivo = obtenerNombreArchivo(uri)
                binding.tvArchivoSeleccionado.text = "Archivo seleccionado: $nombreArchivo"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCargarComprobanteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Obtener datos enviados por Bundle (trabajoId y título)
        val tramiteId = requireArguments().getInt("tramiteId")
        val titulo = requireArguments().getString("tituloTrabajo")

        binding.tvTituloTrabajo.text = "Trabajo: $titulo"

        binding.btnSeleccionarArchivo.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf", "image/*"))
            seleccionarArchivoLauncher.launch(intent)
        }

        binding.btnEnviarComprobante.setOnClickListener {
            val nombre = binding.etNombre.text.toString()
            val apellido = binding.etApellido.text.toString()

            if (nombre.isBlank() || apellido.isBlank() || archivoUri == null) {
                Toast.makeText(requireContext(), "Completá todos los campos y subí el archivo", Toast.LENGTH_SHORT).show()
            } else {
                // Marcamos como pagado el trámite
                TramiteRepository.marcarComoPagado(tramiteId)
                Toast.makeText(requireContext(), "Comprobante enviado", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(nameIndex)
        } ?: "archivo.pdf"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}