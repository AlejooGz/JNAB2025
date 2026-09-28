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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.TextView

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
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_eliminar_novedad, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvTitulo = dialogView.findViewById<TextView>(
            R.id.tvTituloDialog
        )

        val tvMensaje = dialogView.findViewById<TextView>(
            R.id.tvMensajeDialog
        )

        val btnCancelar = dialogView.findViewById<Button>(
            R.id.btnCancelarDialog
        )

        val btnEliminar = dialogView.findViewById<Button>(
            R.id.btnEliminarDialog
        )

        tvTitulo.text = "Eliminar actividad"

        tvMensaje.text =
            "¿Querés eliminar \"${actividad.titulo}\"?"

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnEliminar.setOnClickListener {
            viewModel.eliminarActividad(actividad.id)
            dialog.dismiss()
        }

        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvActividades.adapter = null
        _binding = null
    }
}