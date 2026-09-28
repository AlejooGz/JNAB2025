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
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentHomeOrganizadorBinding
import com.example.jnab2025.ui.viewmodels.HomeOrganizadorViewModel
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeOrganizadorFragment : Fragment() {
    private var _binding: FragmentHomeOrganizadorBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeOrganizadorViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentHomeOrganizadorBinding.inflate(
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
        mostrarSaludo()
        configurarNavegacion()
        observarResumen()
    }

    private fun mostrarSaludo() {
        val nombreCompleto =
            Sesion.nombre(requireContext())
        val primerNombre =
            nombreCompleto
                .trim()
                .split(Regex("\\s+"))
                .firstOrNull()
                .orEmpty()
                .ifBlank {
                    "Organizador"
                }
        binding.tvSaludo.text =
            "¡Hola, $primerNombre!"
    }

    private fun configurarNavegacion() {
        // Resumen
        binding.cardMisSimposios.setOnClickListener {
            abrirMisSimposios()
        }

        binding.cardPropuestasPendientes.setOnClickListener {
            abrirSimposiosFiltrados(
                "PROPUESTAS"
            )
        }
        binding.cardPagosPendientes.setOnClickListener {
            abrirInscriptos()
        }
        binding.cardInscriptos.setOnClickListener {
            abrirInscriptos()
        }
        binding.cardTrabajosSinProgramar.setOnClickListener {
            abrirSimposiosFiltrados(
                "PROGRAMAR"
            )
        }

        // Accesos rapidos
        binding.cardCrearSimposio.setOnClickListener {
            findNavController().navigate(
                R.id.crearSimposioFragment
            )
        }

        binding.cardAccesoMisSimposios.setOnClickListener {
            findNavController().navigate(
                R.id.gestionFaqFragment
            )
        }

        binding.cardVerInscriptos.setOnClickListener {
            findNavController().navigate(
                R.id.crearActividadFragment
            )
        }
        binding.cardPublicarNovedad.setOnClickListener {
            findNavController().navigate(
                R.id.crearNovedadFragment
            )
        }
    }

    private fun abrirMisSimposios() {
        findNavController().navigate(
            R.id.misSimposiosFragment
        )
    }

    private fun abrirInscriptos() {
        findNavController().navigate(
            R.id.verInscriptosFragment
        )
    }

    private fun abrirSimposiosFiltrados(
        tipo: String
    ) {
        val argumentos =
            Bundle().apply {
                putString(
                    "tipoFiltro",
                    tipo
                )
            }
        findNavController().navigate(
            R.id.simposiosFiltroHomeFragment,
            argumentos
        )
    }

    private fun observarResumen() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                launch {
                    viewModel.resumen.collectLatest {
                            resumen ->
                        binding.tvCantidadSimposios.text =
                            resumen
                                .cantidadSimposios
                                .toString()
                        binding.tvPropuestasPendientes.text =
                            resumen
                                .propuestasPendientes
                                .toString()
                        binding.tvPagosPendientes.text =
                            resumen
                                .pagosPendientes
                                .toString()
                        binding.tvCantidadInscriptos.text =
                            resumen
                                .cantidadInscriptos
                                .toString()
                        binding.tvTrabajosSinProgramar.text =
                            resumen
                                .trabajosSinProgramar
                                .toString()
                    }
                }

                launch {
                    viewModel.error.collectLatest {
                            mensaje ->
                        if (mensaje != null) {
                            Toast.makeText(
                                requireContext(),
                                mensaje,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}