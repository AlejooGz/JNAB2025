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
import com.example.jnab2025.utils.mostrarCargando
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CrearNovedadFragment : Fragment() {
    private var _binding: FragmentCrearNovedadBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NovedadesViewModel by viewModels()
    private var imagenUri: Uri? = null
    private val seleccionarImagen =
        registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri == null) { return@registerForActivityResult
            }
            runCatching {
                requireContext()
                    .contentResolver
                    .takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
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
        view: View, savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState
        )
        binding.btnSeleccionarImagen.setOnClickListener {
                seleccionarImagen.launch(arrayOf("image/*")
                )
        }
        binding.btnPublicar.setOnClickListener {
                viewModel.publicar(
                    titulo = binding.etTitulo.text.toString(),
                    descripcion = binding.etDescripcion.text.toString(),
                    imagenUri = imagenUri
                )
        }
        observarEstado()
    }
    private fun observarEstado() {
        viewLifecycleOwner
            .lifecycleScope
            .launch { viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {
                        launch { viewModel
                                .publicando
                                .collectLatest { publicando ->
                                    mostrarCargando(
                                        publicando,
                                        "Publicando…"
                                    )
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
                                            .Publicada -> { Toast
                                                .makeText(
                                                    requireContext(),
                                                    "Novedad publicada",
                                                    Toast.LENGTH_LONG
                                                )
                                                .show()
                                            findNavController()
                                                .popBackStack()
                                        }
                                        is NovedadesViewModel
                                        .Evento
                                        .Error -> { Toast
                                                .makeText(
                                                    requireContext(),
                                                    evento.mensaje,
                                                    Toast.LENGTH_LONG
                                                )
                                                .show()
                                        }
                                    }
                                }
                        }
                    }
            }
    }
    private fun nombreDe(uri: Uri
    ): String {
        val cursor = requireContext()
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
            if (indice >= 0 && it.moveToFirst()
            ) {
                it.getString(indice)
            } else {
                null
            }
        } ?: "Imagen seleccionada"
    }
    override fun onDestroyView() { super.onDestroyView()
        mostrarCargando(false)
        _binding = null
    }
}