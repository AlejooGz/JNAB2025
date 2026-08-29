package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.data.model.FaqFirebase
import com.example.jnab2025.data.model.PublicoFaq
import android.widget.LinearLayout
import com.example.jnab2025.databinding.FragmentGestionFaqBinding
import com.example.jnab2025.ui.adapters.GestionFaqAdapter
import com.example.jnab2025.ui.viewmodels.FaqViewModel
import kotlinx.coroutines.launch

class GestionFaqFragment : Fragment() {
    private var _binding: FragmentGestionFaqBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FaqViewModel by viewModels()
    private lateinit var adapter: GestionFaqAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentGestionFaqBinding.inflate(
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
        configurarRecycler()
        binding.btnNuevaPregunta.setOnClickListener {
            mostrarFormulario()
        }
        observarFaqs()
        observarAvisos()
    }

    private fun configurarRecycler() {
        adapter =
            GestionFaqAdapter(
                onEditarClick = { faq ->
                    mostrarFormulario(faq)
                },
                onEliminarClick = { faq ->
                    confirmarEliminar(faq)
                }
            )
        binding.recyclerFaqGestion.layoutManager =
            LinearLayoutManager(requireContext())
        binding.recyclerFaqGestion.adapter = adapter
    }

    private fun observarFaqs() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.faqs.collect { lista ->
                    adapter.actualizar(lista)
                }
            }
        }
    }
    private fun observarAvisos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
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
    private fun mostrarFormulario(
        faq: FaqFirebase? = null
    ) {
        val layout =
            LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(50, 20, 50, 0)
            }
        val etPregunta =
            EditText(requireContext()).apply {
                hint = "Pregunta"
                setText(faq?.pregunta.orEmpty())
            }
        val etRespuesta =
            EditText(requireContext()).apply {
                hint = "Respuesta"
                minLines = 4
                setText(faq?.respuesta.orEmpty())
            }
        val etOrden =
            EditText(requireContext()).apply {
                hint = "Orden"
                inputType = android.text.InputType.TYPE_CLASS_NUMBER
                setText(
                    faq?.orden
                        ?.toString()
                        ?: "1"
                )
            }
        val opciones = arrayOf("Asistente", "Expositor")
        val seleccionadoInicial =
            if (
                faq?.publico == PublicoFaq.EXPOSITOR.name
            ) {
                1
            } else {
                0
            }
        var publicoSeleccionado = seleccionadoInicial
        layout.addView(etPregunta)
        layout.addView(etRespuesta)
        layout.addView(etOrden)
        AlertDialog.Builder(requireContext())
            .setTitle(
                if (faq == null) { "Nueva pregunta frecuente"
                } else { "Editar pregunta" }
            )
            .setSingleChoiceItems(
                opciones,
                seleccionadoInicial
            ) { _, which ->
                publicoSeleccionado =
                    which
            }
            .setView(layout)
            .setPositiveButton(
                if (faq == null) {
                    "Publicar"
                } else {
                    "Guardar"
                }
            ) { _, _ ->
                val publico =
                    if (
                        publicoSeleccionado == 0
                    ) {
                        PublicoFaq.ASISTENTE
                    } else {
                        PublicoFaq.EXPOSITOR
                    }
                val orden = etOrden
                        .text
                        .toString()
                        .toIntOrNull()
                        ?: 0
                if (faq == null) {
                    viewModel.crear(
                        publico = publico,
                        pregunta = etPregunta.text.toString(),
                        respuesta = etRespuesta.text.toString(),
                        orden = orden
                    )
                } else {
                    viewModel.editar(
                        faq = faq,
                        publico = publico,
                        pregunta = etPregunta.text.toString(),
                        respuesta = etRespuesta.text.toString(),
                        orden = orden
                    )
                }
            }
            .setNegativeButton(
                "Cancelar",
                null
            )
            .show()
    }
    private fun confirmarEliminar(
        faq: FaqFirebase
    ) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar pregunta")
            .setMessage(
                "¿Querés dejar de mostrar esta pregunta?"
            )
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminar(faq)
            }
            .setNegativeButton(
                "Cancelar",
                null
            )
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}