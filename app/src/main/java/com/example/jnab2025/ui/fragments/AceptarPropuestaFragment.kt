package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jnab2025.databinding.FragmentAceptarPropuestaBinding
import com.example.jnab2025.ui.viewmodels.PropuestasViewModel
import com.example.jnab2025.utils.Archivos
import com.example.jnab2025.utils.mostrarCargando
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AceptarPropuestaFragment : Fragment() {
    private var _binding: FragmentAceptarPropuestaBinding? = null
    private val binding get() = _binding!!
    private val args: AceptarPropuestaFragmentArgs by navArgs()
    private val viewModel: PropuestasViewModel by viewModels()
    private val formatoFecha =
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentAceptarPropuestaBinding.inflate(
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
        super.onViewCreated(view, savedInstanceState)

        viewModel.cargarTrabajo(
            args.trabajoId
        )
        binding.btnVerPdf.setOnClickListener {
            val trabajo = viewModel.trabajo.value
            Archivos.mensajeDe(
                Archivos.abrirPdf(
                    requireContext(),
                    trabajo?.archivoUrl
                )
            )?.let { mensaje ->

                avisar(mensaje)
            }
        }

        binding.btnCancelar.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnConfirmar.setOnClickListener {
            viewModel.aprobar(
                args.trabajoId
            )
        }

        observarDatos()
    }

    private fun observarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                launch {

                    viewModel.trabajo.collectLatest { trabajo ->

                        binding.tvTituloTrabajo.text =
                            trabajo?.titulo.orEmpty()

                        binding.btnVerPdf.text =
                            trabajo
                                ?.nombreArchivo
                                ?.let {
                                    "Ver PDF: $it"
                                }
                                ?: "Ver PDF adjunto"
                    }
                }

                launch {

                    viewModel.simposio.collectLatest { simposio ->

                        simposio
                            ?: return@collectLatest

                        val fechaInicio =
                            simposio.fechaInicio
                                ?.toDate()

                        val fechaFin =
                            simposio.fechaFin
                                ?.toDate()

                        binding.tvSimposio.text =
                            buildString {

                                append(
                                    simposio.titulo
                                )

                                if (
                                    fechaInicio != null &&
                                    fechaFin != null
                                ) {

                                    val desde =
                                        formatoFecha.format(
                                            fechaInicio
                                        )

                                    val hasta =
                                        formatoFecha.format(
                                            fechaFin
                                        )

                                    if (desde == hasta) {

                                        append(
                                            "\nFecha: $desde"
                                        )

                                    } else {

                                        append(
                                            "\nDel $desde al $hasta"
                                        )
                                    }
                                }

                                append(
                                    "\n${simposio.aulaNombre}"
                                )
                            }
                    }
                }

                launch {
                    viewModel.enviando.collectLatest { mostrarCargando(it) }
                }

                launch {
                    viewModel.avisos.collectLatest { mensaje ->
                        avisar(mensaje)
                    }
                }

                launch {
                    viewModel.resueltas.collectLatest {
                        findNavController()
                            .popBackStack()
                    }
                }
            }
        }
    }

    private fun avisar(
        mensaje: String
    ) {

        Toast.makeText(
            requireContext(),
            mensaje,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mostrarCargando(false)
        _binding = null
    }
}