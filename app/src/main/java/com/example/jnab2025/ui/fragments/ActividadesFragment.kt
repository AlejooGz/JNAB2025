package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.R
import com.example.jnab2025.data.model.ActividadFirebase
import com.example.jnab2025.databinding.FragmentActividadesBinding
import com.example.jnab2025.ui.adapters.ActividadAdapter
import com.example.jnab2025.ui.viewmodels.ActividadViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ActividadesFragment : Fragment() {
    private var _binding: FragmentActividadesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ActividadViewModel by viewModels()
    private lateinit var adapter: ActividadAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentActividadesBinding.inflate(
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

        configurarRecyclerView()

        binding.btnNuevaActividad.setOnClickListener {

            findNavController().navigate(
                ActividadesFragmentDirections
                    .actionActividadesFragmentToCrearActividadFragment()
            )
        }

        observarDatos()
    }

    private fun configurarRecyclerView() {

        adapter =
            ActividadAdapter(

                onEditarClick = { actividad ->

                    val accion =
                        ActividadesFragmentDirections
                            .actionActividadesFragmentToEditarActividadFragment(
                                actividad.id
                            )

                    findNavController()
                        .navigate(accion)
                },

                onEliminarClick = { actividad ->

                    confirmarEliminar(
                        actividad
                    )
                }
            )

        binding.rvActividades.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        binding.rvActividades.adapter =
            adapter
    }

    private fun observarDatos() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                launch {

                    viewModel.actividades.collectLatest {
                            actividades ->

                        adapter.submitList(

                            actividades.sortedWith(

                                compareBy(

                                    {
                                        it.fecha
                                            ?.toDate()
                                            ?.time
                                            ?: Long.MAX_VALUE
                                    },

                                    {
                                        it.horaInicio
                                    }
                                )
                            )
                        )

                        binding.tvSinActividades.visibility =
                            if (actividades.isEmpty()) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                    }
                }

                launch {

                    viewModel.avisos.collectLatest {
                            mensaje ->

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

    private fun confirmarEliminar(
        actividad: ActividadFirebase
    ) {

        val dialog =
            AlertDialog.Builder(
                requireContext()
            )
                .setTitle(
                    "Eliminar actividad"
                )
                .setMessage(
                    "¿Querés eliminar \"${actividad.titulo}\"?"
                )
                .setNegativeButton(
                    "Cancelar",
                    null
                )
                .setPositiveButton(
                    "Eliminar"
                ) { _, _ ->

                    viewModel.eliminarActividad(
                        actividad.id
                    )
                }
                .create()

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawableResource(
                R.drawable.bg_dialog_cream
            )
            dialog.findViewById<android.widget.TextView>(
                androidx.appcompat.R.id.alertTitle
            )?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dark_red
                )
            )
            dialog.findViewById<android.widget.TextView>(
                android.R.id.message
            )?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.dark_red
                )
            )

            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).apply {
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                setBackgroundResource(
                    R.drawable.bg_button_dark_red
                )
                isAllCaps = false
            }

            dialog.getButton(
                AlertDialog.BUTTON_NEGATIVE
            ).apply {
                setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.dark_red
                    )
                )
                setBackgroundColor(
                    android.graphics.Color.TRANSPARENT
                )
                isAllCaps = false
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvActividades.adapter = null
        _binding = null
    }
}