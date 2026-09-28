package com.example.jnab2025.ui.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.R
import com.example.jnab2025.data.model.CategoriaInscripcion
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.InscriptoSeguimientoFirebase
import com.example.jnab2025.data.model.TipoInscripcion
import com.example.jnab2025.databinding.FragmentVerInscriptosBinding
import com.example.jnab2025.ui.adapters.InscriptosAdapter
import com.example.jnab2025.ui.viewmodels.FiltroInscriptosViewModel
import com.example.jnab2025.ui.viewmodels.InscriptosViewModel
import com.example.jnab2025.utils.Archivos
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class VerInscriptosFragment : Fragment() {
    private var _binding: FragmentVerInscriptosBinding? = null
    private val binding get() = _binding!!
    private val viewModel: InscriptosViewModel by viewModels()
    private lateinit var filtroViewModel: FiltroInscriptosViewModel
    private lateinit var adapter: InscriptosAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        filtroViewModel =
            ViewModelProvider(requireActivity())[
                FiltroInscriptosViewModel::class.java
            ]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentVerInscriptosBinding.inflate(
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
        adapter = InscriptosAdapter(
                onVerComprobante = { seguimiento ->
                    val resultado =
                        Archivos.abrirPdf(
                            requireContext(),
                            seguimiento
                                .comprobante
                                ?.archivoUrl
                        )
                    Archivos
                        .mensajeDe(resultado)
                        ?.let { mensaje ->
                            Toast.makeText(
                                requireContext(),
                                mensaje,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                onVerificar = { seguimiento ->
                    viewModel.verificar(
                        seguimiento
                    )
                },
                onRechazar = { seguimiento ->
                    confirmarRechazo(
                        seguimiento
                    )
                }
            )
        binding.recyclerInscriptos.layoutManager =
            LinearLayoutManager(
                requireContext()
            )
        binding.recyclerInscriptos.adapter =
            adapter
        observarDatos()
    }
    private fun observarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch {
                    combine(
                        viewModel.inscriptos,
                        filtroViewModel.estadosSeleccionados,
                        filtroViewModel.tiposSeleccionados,
                        filtroViewModel.categoriasSeleccionadas
                    ) {
                            inscriptos,
                            estados,
                            tipos,
                            categorias ->

                        filtrarInscriptos(
                            inscriptos,
                            estados,
                            tipos,
                            categorias
                        )
                    }.collect { inscriptosFiltrados ->
                        adapter.submitList(
                            inscriptosFiltrados
                        )
                        binding.tvSinInscriptos.visibility =
                            if (inscriptosFiltrados.isEmpty()) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                    }
                }
                launch {
                    viewModel.avisos.collect { mensaje ->
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
    private fun filtrarInscriptos(
        inscriptos: List<InscriptoSeguimientoFirebase>,
        estados: Set<EstadoComprobante>,
        tipos: Set<TipoInscripcion>,
        categorias: Set<CategoriaInscripcion>
    ): List<InscriptoSeguimientoFirebase> {
        return inscriptos.filter { seguimiento ->
            val inscripcion = seguimiento.inscripcion
            val comprobante = seguimiento.comprobante
            val coincideEstado =
                estados.isEmpty() ||
                        estados.any { estado ->
                            if (comprobante == null) {
                                // un usuario sin comprobante se considera pendiente.
                                estado == EstadoComprobante.PENDIENTE
                            } else {
                                comprobante.estado == estado.name
                            }
                        }

            val coincideTipo =
                tipos.isEmpty() ||
                        tipos.any { tipo ->
                            inscripcion.tipo == tipo.name
                        }

            val coincideCategoria =
                categorias.isEmpty() ||
                        categorias.any { categoria ->
                            inscripcion.categoria == categoria.name
                        }
            coincideEstado && coincideTipo && coincideCategoria
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        inflater.inflate(
            R.menu.menu_inscriptos,
            menu
        )
        super.onCreateOptionsMenu(
            menu,
            inflater
        )
    }

    override fun onOptionsItemSelected(
        item: MenuItem
    ): Boolean {
        return when (item.itemId) {
            R.id.action_filtrar_inscriptos -> {
                FiltroInscriptosBottomSheetFragment()
                    .show(
                        parentFragmentManager,
                        "FiltroInscriptosBottomSheet"
                    )
                true
            }
            else -> super.onOptionsItemSelected(
                item
            )
        }
    }
    private fun confirmarRechazo(
        seguimiento: InscriptoSeguimientoFirebase
    ) {
        val dialogView =
            LayoutInflater.from(requireContext())
                .inflate(
                    R.layout.dialog_eliminar_novedad,
                    null
                )
        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setView(dialogView)
                .create()

        val tvTitulo =
            dialogView.findViewById<TextView>(
                R.id.tvTituloDialog
            )

        val tvMensaje =
            dialogView.findViewById<TextView>(
                R.id.tvMensajeDialog
            )

        val btnCancelar =
            dialogView.findViewById<Button>(
                R.id.btnCancelarDialog
            )

        val btnEliminar =
            dialogView.findViewById<Button>(
                R.id.btnEliminarDialog
            )

        tvTitulo.text = "Rechazar inscripción"
        tvMensaje.text = "¿Querés rechazar esta inscripción?"

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }
        btnEliminar.text = "Rechazar"

        btnEliminar.setOnClickListener {
            viewModel.rechazar(
                seguimiento
            )
            dialog.dismiss()
        }
        dialog.show()

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerInscriptos.adapter = null
        _binding = null
    }
}