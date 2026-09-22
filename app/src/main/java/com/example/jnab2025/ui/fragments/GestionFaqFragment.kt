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
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button
import android.widget.TextView
import com.example.jnab2025.R
import android.widget.RadioButton


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

        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_faq, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        val tvTitulo = dialogView.findViewById<TextView>(
            R.id.tvTituloFaqDialog
        )

        val etPregunta = dialogView.findViewById<EditText>(
            R.id.etPreguntaDialog
        )

        val etRespuesta = dialogView.findViewById<EditText>(
            R.id.etRespuestaDialog
        )

        val etOrden = dialogView.findViewById<EditText>(
            R.id.etOrdenDialog
        )

        val rbAsistente = dialogView.findViewById<RadioButton>(
            R.id.rbAsistente
        )

        val rbExpositor = dialogView.findViewById<RadioButton>(
            R.id.rbExpositor
        )

        val btnCancelar = dialogView.findViewById<Button>(
            R.id.btnCancelarFaq
        )

        val btnGuardar = dialogView.findViewById<Button>(
            R.id.btnGuardarFaq
        )

        val esNueva = faq == null

        tvTitulo.text =
            if (esNueva) {
                "Nueva pregunta frecuente"
            } else {
                "Editar pregunta"
            }

        btnGuardar.text =
            if (esNueva) {
                "Publicar"
            } else {
                "Guardar"
            }

        etPregunta.setText(faq?.pregunta.orEmpty())
        etRespuesta.setText(faq?.respuesta.orEmpty())
        etOrden.setText(
            faq?.orden?.toString() ?: "1"
        )

        if (faq?.publico == PublicoFaq.EXPOSITOR.name) {
            rbExpositor.isChecked = true
        } else {
            rbAsistente.isChecked = true
        }

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardar.setOnClickListener {

            val publico =
                if (rbExpositor.isChecked) {
                    PublicoFaq.EXPOSITOR
                } else {
                    PublicoFaq.ASISTENTE
                }

            val orden =
                etOrden.text
                    .toString()
                    .toIntOrNull()
                    ?: 0

            if (esNueva) {

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

            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
    }
    private fun confirmarEliminar(faq: FaqFirebase) {

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

        tvTitulo.text = "Eliminar pregunta"

        tvMensaje.text =
            "¿Querés dejar de mostrar esta pregunta?"

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnEliminar.setOnClickListener {
            viewModel.eliminar(faq)
            dialog.dismiss()
        }

        dialog.show()

        dialog.window?.setBackgroundDrawable(
            ColorDrawable(Color.TRANSPARENT)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}