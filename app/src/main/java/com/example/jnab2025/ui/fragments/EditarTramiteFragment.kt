package com.example.jnab2025.ui.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.*
import android.widget.Toast
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentEditarTramiteBinding
import com.example.jnab2025.ui.viewmodels.CharlaViewModel

class EditarTramiteFragment : Fragment() {

    private var _binding: FragmentEditarTramiteBinding? = null
    private val binding get() = _binding!!

    private var archivoUri: Uri? = null
    private var nombreArchivo: String = "Sin archivo"
    private var tramiteId: Int = -1

    private val charlaViewModel: CharlaViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tramiteId = arguments?.getInt("tramiteId") ?: -1

        val tramite = charlaViewModel.charlas.value?.find { it.id == tramiteId }

        Log.d("EditarTramiteFragment", "tramiteId recibido: $tramiteId")
        charlaViewModel.charlas.value?.forEach {
            Log.d("EditarTramiteFragment", "Charla en VM: id=${it.id}, titulo=${it.titulo}")
        }

        if (tramite == null) {
            Toast.makeText(requireContext(), "Trámite no encontrado", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        binding.etTitulo.setText(tramite.titulo)
        binding.etResumen.setText(tramite.descripcion ?: "")
        binding.tvArchivoSeleccionado.text = "Archivo: ${tramite.nombreArchivo}"
        nombreArchivo = tramite.nombreArchivo

        binding.btnSeleccionarArchivo.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            seleccionarArchivo.launch(intent)
        }

        binding.btnGuardarCambios.setOnClickListener {
            val nuevoTitulo = binding.etTitulo.text.toString()
            val nuevoResumen = binding.etResumen.text.toString()

            if (nuevoTitulo.isBlank() || nuevoResumen.isBlank()) {
                Toast.makeText(requireContext(), "Completá todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            charlaViewModel.editarCharla(tramite, nuevoTitulo, nuevoResumen, nombreArchivo)

            Toast.makeText(requireContext(), "Trámite actualizado", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.seguimientoTramiteFragment)
        }
    }

    private val seleccionarArchivo = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            archivoUri = it.data?.data
            archivoUri?.let { uri ->
                nombreArchivo = obtenerNombreArchivo(uri)
                binding.tvArchivoSeleccionado.text = "Archivo: $nombreArchivo"
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentEditarTramiteBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun obtenerNombreArchivo(uri: Uri): String {
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            it.getString(index)
        } ?: "archivo.pdf"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
