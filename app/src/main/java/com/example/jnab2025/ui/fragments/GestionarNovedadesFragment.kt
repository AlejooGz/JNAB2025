package com.example.jnab2025.ui.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentGestionarNovedadesBinding
import com.example.jnab2025.data.model.NovedadFirebase
import com.example.jnab2025.ui.adapters.NovedadesAdapter
import com.example.jnab2025.ui.viewmodels.NovedadesViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.graphics.Color
import android.widget.Button
import android.widget.TextView
import android.graphics.drawable.ColorDrawable


class GestionarNovedadesFragment : Fragment() {
    private var _binding: FragmentGestionarNovedadesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NovedadesViewModel by viewModels()
    private lateinit var adapter: NovedadesAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionarNovedadesBinding.inflate(
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
        configurarBotonCrear()
        observarNovedades()
    }

    private fun configurarRecyclerView() {
        adapter = NovedadesAdapter(
            esOrganizador = true,
            onEditar = { novedad ->
                editarNovedad(novedad)
            },
            onEliminar = { novedad ->
                confirmarEliminacion(novedad)
            }
        )
        binding.recyclerViewNovedades.apply {
            layoutManager =
                LinearLayoutManager(requireContext())
            adapter =
                this@GestionarNovedadesFragment.adapter
        }
    }
    private fun configurarBotonCrear() {
        binding.btnCrearNovedad.setOnClickListener {
            findNavController().navigate(
                R.id.crearNovedadFragment
            )
        }
    }
    private fun editarNovedad(
        novedad: NovedadFirebase
    ) {
        val bundle = bundleOf(
            "novedadId" to novedad.id
        )
        findNavController().navigate(
            R.id.crearNovedadFragment,
            bundle
        )
    }
    private fun confirmarEliminacion(novedad: NovedadFirebase) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_eliminar_novedad, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()
        val tvMensaje = dialogView.findViewById<TextView>(
            R.id.tvMensajeDialog
        )
        val btnCancelar = dialogView.findViewById<Button>(
            R.id.btnCancelarDialog
        )
        val btnEliminar = dialogView.findViewById<Button>(
            R.id.btnEliminarDialog
        )
        tvMensaje.text =
            "¿Querés eliminar la novedad \"${novedad.titulo}\"?"
        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }
        btnEliminar.setOnClickListener {
            viewModel.eliminar(novedad)
            dialog.dismiss()
        }
        dialog.setOnShowListener {
            dialog.window?.setBackgroundDrawable(
                ColorDrawable(Color.TRANSPARENT)
            )
        }
        dialog.show()
    }
    private fun observarNovedades() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                launch {
                    viewModel.novedades.collectLatest { novedades ->
                        adapter.submitList(novedades)
                    }
                }
                launch {
                    viewModel.eventos.collectLatest { evento ->
                        when (evento) {
                            is NovedadesViewModel
                            .Evento.Error -> {
                                Toast.makeText(
                                    requireContext(),
                                    evento.mensaje,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            is NovedadesViewModel
                            .Evento.Eliminada -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Novedad eliminada",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is NovedadesViewModel
                            .Evento.Actualizada -> {
                                Toast.makeText(
                                    requireContext(),
                                    "Novedad actualizada",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else -> { //ls publicscion se maneja desde CrearNovedadFragment
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewNovedades.adapter = null
        _binding = null
    }
}