package com.example.jnab2025.ui.fragments

import android.app.Activity
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentTramiteExpositorBinding
import com.example.jnab2025.models.Tramite
import com.example.jnab2025.data.TramiteRepository // o donde esté tu repositorio

class TramiteExpositorFragment : Fragment() {

    private var _binding: FragmentTramiteExpositorBinding? = null
    private val binding get() = _binding!!

    private var archivoSeleccionadoUri: Uri? = null
    private var nombreArchivoSeleccionado: String = "Sin archivo"

    private val seleccionarArchivoLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            archivoSeleccionadoUri = result.data?.data
            archivoSeleccionadoUri?.let { uri ->
                nombreArchivoSeleccionado = obtenerNombreArchivo(uri)
                binding.tvArchivoSeleccionado.text = "Archivo: $nombreArchivoSeleccionado"
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTramiteExpositorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnSeleccionarArchivo.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/pdf"
            seleccionarArchivoLauncher.launch(intent)
        }

        binding.btnEnviarTramite.setOnClickListener {
            val titulo = binding.etTituloTrabajo.text.toString()
            val resumen = binding.etResumenTrabajo.text.toString()

            if (titulo.isBlank() || resumen.isBlank() || archivoSeleccionadoUri == null) {
                Toast.makeText(requireContext(), "Completá todos los campos y seleccioná un archivo", Toast.LENGTH_SHORT).show()
            } else {
                val nuevo = TramiteRepository.agregarTramite(
                    titulo = titulo,
                    nombreArchivo = nombreArchivoSeleccionado
                )

                Toast.makeText(requireContext(), "Trámite enviado correctamente", Toast.LENGTH_SHORT).show()

                // Redirigir al home (MainFragment)
                findNavController().navigate(
                    R.id.seguimientoTramiteFragment,
                    null,
                    NavOptions.Builder()
                        .setPopUpTo(R.id.mainFragment, false) // deja MainFragment en el backstack
                        .build()
                )
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
