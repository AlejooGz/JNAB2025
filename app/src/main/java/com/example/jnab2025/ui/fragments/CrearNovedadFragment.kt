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
import com.example.jnab2025.databinding.FragmentCrearNovedadBinding
import com.example.jnab2025.ui.viewmodels.NovedadesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CrearNovedadFragment : Fragment() {
    private var _binding: FragmentCrearNovedadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NovedadesViewModel by viewModels()
    private var imagenUri: Uri? = null
    private var imagenActualUrl: String? = null

    // Si está vacío estamos creando
    // Si tiene valor estamos editando
    private val novedadId: String
        get() = arguments?.getString("novedadId").orEmpty()

    private val modoEdicion: Boolean
        get() = novedadId.isNotBlank()

    private val seleccionarImagen =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->

            if (uri == null) {
                return@registerForActivityResult
            }
            runCatching {
                requireContext()
                    .contentResolver
                    .takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
            }

            imagenUri = uri
            binding.ivVistaPrevia.setImageURI(uri)
            binding.ivVistaPrevia.visibility = View.VISIBLE
            binding.tvImagenSeleccionada.text = nombreDe(uri)
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearNovedadBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        configurarPantalla()
        if (modoEdicion) {
            cargarNovedad()
        }
        binding.btnSeleccionarImagen.setOnClickListener {
            seleccionarImagen.launch(
                arrayOf("image/*")
            )
        }

        binding.btnPublicar.setOnClickListener {
            guardarNovedad()
        }
        observarEstado()
    }
    private fun configurarPantalla() {
        if (modoEdicion) {
            binding.tvTituloPantalla.text =
                "Editar novedad"
            binding.btnPublicar.text =
                "Guardar cambios"

        } else {
            binding.tvTituloPantalla.text =
                "Publicar novedad"
            binding.btnPublicar.text =
                "Publicar novedad"
        }
    }
    private fun cargarNovedad() {
        viewModel.obtenerNovedad(novedadId) { novedad ->
            if (!isAdded || _binding == null) {
                return@obtenerNovedad
            }
            if (novedad == null) {
                Toast.makeText(
                    requireContext(),
                    "No se encontró la novedad",
                    Toast.LENGTH_LONG
                ).show()
                findNavController().popBackStack()
                return@obtenerNovedad
            }
            binding.etTitulo.setText(novedad.titulo
            )
            binding.etDescripcion.setText(novedad.descripcion
            )
            imagenActualUrl = novedad.imagenUrl

            if (!novedad.imagenUrl.isNullOrBlank()) {
                binding.ivVistaPrevia.visibility =
                    View.VISIBLE
                com.bumptech.glide.Glide
                    .with(requireContext())
                    .load(novedad.imagenUrl)
                    .centerCrop()
                    .into(binding.ivVistaPrevia)
                binding.tvImagenSeleccionada.text =
                    "Imagen actual"
            }
        }
    }
    private fun guardarNovedad() {
        val titulo = binding.etTitulo.text.toString()
        val descripcion = binding.etDescripcion.text.toString()

        if (modoEdicion) {
            viewModel.actualizar(
                novedadId = novedadId,
                titulo = titulo,
                descripcion = descripcion,
                imagenUri = imagenUri,
                imagenActualUrl = imagenActualUrl
            )
        } else {
            viewModel.publicar(
                titulo = titulo,
                descripcion = descripcion,
                imagenUri = imagenUri
            )
        }
    }
    private fun observarEstado() {
        viewLifecycleOwner
            .lifecycleScope
            .launch {
                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {
                        launch {
                            viewModel
                                .publicando
                                .collectLatest { publicando ->
                                    binding
                                        .progressPublicar
                                        .visibility =
                                        if (publicando) {
                                            View.VISIBLE
                                        } else {
                                            View.GONE
                                        }
                                    binding
                                        .btnPublicar
                                        .isEnabled =
                                        !publicando
                                    binding
                                        .btnSeleccionarImagen
                                        .isEnabled =
                                        !publicando
                                }
                        }
                        launch {
                            viewModel
                                .eventos
                                .collectLatest { evento ->
                                    when (evento) {
                                        NovedadesViewModel
                                            .Evento
                                            .Publicada -> {
                                            Toast.makeText(
                                                requireContext(),
                                                "Novedad publicada",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            findNavController()
                                                .popBackStack()
                                        }
                                        NovedadesViewModel
                                            .Evento
                                            .Actualizada -> {
                                            Toast.makeText(
                                                requireContext(),
                                                "Novedad actualizada",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            findNavController()
                                                .popBackStack()
                                        }
                                        is NovedadesViewModel
                                        .Evento
                                        .Error -> {
                                            Toast.makeText(
                                                requireContext(),
                                                evento.mensaje,
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        else -> { // No hacer nada
                                        }
                                    }
                                }
                        }
                    }
            }
    }
    private fun nombreDe(
        uri: Uri
    ): String {
        val cursor =
            requireContext()
                .contentResolver
                .query(
                    uri,
                    null,
                    null,
                    null,
                    null
                )
        return cursor?.use {
            val indice = it.getColumnIndex(
                    OpenableColumns.DISPLAY_NAME
                )
            if (
                indice >= 0 && it.moveToFirst()
            ) {
                it.getString(indice)
            } else { null
            }
        } ?: "Imagen seleccionada"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}