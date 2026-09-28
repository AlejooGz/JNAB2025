package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.R
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.TrabajoSeguimientoFirebase
import com.example.jnab2025.databinding.FragmentSeguimientoTramiteBinding
import com.example.jnab2025.ui.adapters.MisTrabajosAdapter
import com.example.jnab2025.ui.viewmodels.FiltroTrabajosViewModel
import com.example.jnab2025.ui.viewmodels.MisTrabajosViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch


class SeguimientoTramiteFragment : Fragment() {

    private var _binding: FragmentSeguimientoTramiteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MisTrabajosViewModel by viewModels()

    private lateinit var filtroViewModel: FiltroTrabajosViewModel

    private lateinit var adapter: MisTrabajosAdapter

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)

        filtroViewModel =
            ViewModelProvider(requireActivity())
                .get(FiltroTrabajosViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentSeguimientoTramiteBinding.inflate(
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

        if (!viewModel.haySesion) {
            Toast.makeText(
                requireContext(),
                "Inicia sesion para ver tus trabajos",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        adapter = MisTrabajosAdapter(
            onClick = { seguimiento ->
                Toast.makeText(
                    requireContext(),
                    seguimiento.trabajo.titulo,
                    Toast.LENGTH_SHORT
                ).show()
            },
            onPagoClick = {
                findNavController().navigate(
                    R.id.action_seguimientoTramiteFragment_to_inscripcionFragment
                )
            }
        )

        binding.recyclerViewTramites.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerViewTramites.adapter = adapter

        observarDatos()
    }

    private fun observarDatos() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                launch {

                    combine(
                        viewModel.trabajos,
                        filtroViewModel.estadosSeleccionados,
                        filtroViewModel.programacionSeleccionada
                    ) {
                            trabajos,
                            estados,
                            programacion ->

                        filtrarTrabajos(
                            trabajos = trabajos,
                            estados = estados,
                            programacion = programacion
                        )

                    }.collectLatest { trabajosFiltrados ->

                        adapter.submitList(
                            trabajosFiltrados
                        )
                    }
                }

                launch {

                    viewModel.error.collectLatest { mensaje ->

                        mensaje?.let {
                            Toast.makeText(
                                requireContext(),
                                it,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun filtrarTrabajos(
        trabajos: List<TrabajoSeguimientoFirebase>,
        estados: Set<EstadoTrabajo>,
        programacion:
        Set<FiltroTrabajosViewModel.FiltroProgramacion>
    ): List<TrabajoSeguimientoFirebase> {

        return trabajos.filter { seguimiento ->

            val trabajo = seguimiento.trabajo

            // Filtro por estado
            val coincideEstado =
                estados.isEmpty() ||
                        estados.any { estado ->
                            trabajo.estado == estado.name
                        }

            // Un trabajo está programado si:
            // - está APROBADO
            // - tiene fecha
            // - tiene hora de inicio
            val estaProgramado =
                trabajo.estado == EstadoTrabajo.APROBADO.name &&
                        seguimiento.fecha != null &&
                        seguimiento.horaInicio != null

            // Filtro por programación
            val coincideProgramacion =
                when {

                    programacion.isEmpty() ->
                        true

                    programacion.size == 2 ->
                        true

                    FiltroTrabajosViewModel
                        .FiltroProgramacion
                        .PROGRAMADO in programacion ->
                        estaProgramado

                    FiltroTrabajosViewModel
                        .FiltroProgramacion
                        .SIN_PROGRAMAR in programacion ->
                        !estaProgramado

                    else ->
                        true
                }

            coincideEstado &&
                    coincideProgramacion
        }
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater
    ) {
        inflater.inflate(
            R.menu.menu_trabajos,
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

            R.id.action_filtrar_trabajos -> {

                FiltroTrabajosBottomSheetFragment()
                    .show(
                        parentFragmentManager,
                        "FiltroTrabajosBottomSheet"
                    )

                true
            }

            else ->
                super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        binding.recyclerViewTramites.adapter = null
        super.onDestroyView()
        _binding = null
    }
}