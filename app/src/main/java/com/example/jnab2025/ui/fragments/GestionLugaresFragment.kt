package com.example.jnab2025.ui.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.R
import com.example.jnab2025.data.model.CategoriaLugar
import com.example.jnab2025.data.model.LugarFirebase
import com.example.jnab2025.databinding.DialogLugarBinding
import com.example.jnab2025.databinding.FragmentGestionLugaresBinding
import com.example.jnab2025.ui.adapters.GestionLugaresAdapter
import com.example.jnab2025.ui.viewmodels.LugaresViewModel
import com.example.jnab2025.utils.mostrarCargando
import kotlinx.coroutines.launch
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Button

class GestionLugaresFragment : Fragment() {
    private var _binding: FragmentGestionLugaresBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LugaresViewModel by viewModels()
    private lateinit var adapter: GestionLugaresAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGestionLugaresBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GestionLugaresAdapter(
            onEditar = { lugar -> mostrarFormulario(lugar)
            },
            onCambiarEstado = { lugar -> confirmarCambioEstado(lugar)
            }
        )
        binding.recyclerLugares.layoutManager = LinearLayoutManager(
                requireContext()
        )
        binding.recyclerLugares.adapter = adapter
        binding.btnNuevoLugar.setOnClickListener { mostrarFormulario() }
        observarLugares()
        observarAvisos()
        observarEnvio()

        //se ejecutó una sola vez
        /*viewModel.cargarLugaresIniciales { ok, mensaje ->

            Toast.makeText(
                requireContext(),
                mensaje,
                Toast.LENGTH_LONG
            ).show()
        }*/
    }
    private fun observarLugares() {
        viewLifecycleOwner.lifecycleScope
            .launch {
                viewLifecycleOwner.repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {
                        viewModel.todosLugares
                            .collect {
                                adapter.actualizar(it)
                            }
                    }
            }
    }

    private fun observarAvisos() {
        viewLifecycleOwner.lifecycleScope
            .launch {
                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {
                        viewModel.avisos
                            .collect {
                                Toast.makeText(
                                    requireContext(),
                                    it,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
            }
    }
    private fun observarEnvio() {
        viewLifecycleOwner.lifecycleScope
            .launch {
                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {
                        viewModel.enviando
                            .collect {
                                mostrarCargando(it, "Guardando…")
                            }
                    }
            }
    }
    private fun mostrarFormulario(
        lugar: LugarFirebase? = null
    ) {
        val dialogBinding = DialogLugarBinding.inflate(layoutInflater)
        val categorias = arrayOf("Hospedaje", "Restaurante", "Agencia")
        val adapterCategoria = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner,
            categorias
        ).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }

        dialogBinding.spCategoria.adapter = adapterCategoria

        if (lugar != null) {
            dialogBinding.etNombre.setText(lugar.nombre)
            dialogBinding.etDescuento.setText(lugar.descuento)
            dialogBinding.etDireccion.setText(lugar.direccion)
            dialogBinding.etTelefono.setText(lugar.telefono)
            dialogBinding.etEmail.setText(lugar.email)
            dialogBinding.etWeb.setText(lugar.web)
            dialogBinding.etLatitud.setText(lugar.latitud.toString())
            dialogBinding.etLongitud.setText(lugar.longitud.toString())
            dialogBinding.spCategoria.setSelection(
                when (lugar.categoriaEnum()
                ) {
                    CategoriaLugar.HOSPEDAJE -> 0
                    CategoriaLugar.RESTAURANTE -> 1
                    CategoriaLugar.AGENCIA -> 2
                }
            )
        }
        val dialog = AlertDialog.Builder(requireContext()
        )
            .setTitle(
                if (lugar == null) {
                    "Nuevo lugar"
                } else {
                    "Editar lugar"
                }
            )
            .setView(dialogBinding.root)
            .setNegativeButton(
                "Cancelar",
                null
            )
            .setPositiveButton(
                "Guardar",
                null
            )
            .create()
        dialog.setOnShowListener {
            val darkRed = ContextCompat.getColor(
                    requireContext(),
                    R.color.dark_red
                )
            dialog.window
                ?.setBackgroundDrawableResource(
                    R.drawable.bg_dialog_cream
                )
            dialog.findViewById<TextView>(
                androidx.appcompat.R.id.alertTitle
            )?.apply {
                setTextColor(darkRed)
                setTypeface(typeface, Typeface.BOLD)
            }
            val btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            val btnCancelar = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            btnGuardar.setTextColor(darkRed)
            btnCancelar.setTextColor(darkRed)
            btnGuardar.setOnClickListener {
                val nombre = dialogBinding.etNombre.text.toString().trim()
                val latitud = dialogBinding.etLatitud.text.toString()
                        .replace(
                            ",",
                            "."
                        )
                        .toDoubleOrNull()
                val longitud = dialogBinding.etLongitud.text.toString()
                        .replace(
                            ",",
                            "."
                        )
                        .toDoubleOrNull()
                if (nombre.isBlank()) {
                    dialogBinding.etNombre.error =
                        "Ingresá un nombre"
                    return@setOnClickListener
                }
                if (latitud == null) {
                    dialogBinding.etLatitud.error =
                        "Latitud inválida"
                    return@setOnClickListener
                }

                if (longitud == null) {
                    dialogBinding.etLongitud.error =
                        "Longitud inválida"
                    return@setOnClickListener
                }

                val categoria =
                    when (
                        dialogBinding
                            .spCategoria
                            .selectedItemPosition
                    ) {
                        0 -> CategoriaLugar.HOSPEDAJE
                        1 -> CategoriaLugar.RESTAURANTE
                        else -> CategoriaLugar.AGENCIA
                    }

                val resultado = LugarFirebase(
                    id = lugar?.id ?: "",
                    nombre = nombre,
                    categoria = categoria.name,
                    latitud = latitud,
                    longitud = longitud,
                    descuento = dialogBinding.etDescuento.text.toString().trim(),
                    direccion = dialogBinding.etDireccion.text.toString().trim(),
                    telefono = dialogBinding.etTelefono.text.toString().trim(),
                    email = dialogBinding.etEmail.text.toString().trim(),
                    web = dialogBinding.etWeb.text.toString().trim(),
                    activo = lugar?.activo ?: true
                    )
                if (lugar == null) {
                    viewModel.crearLugar(resultado)
                } else {
                    viewModel.editarLugar(resultado)
                }
                dialog.dismiss()
            }
        }
        dialog.show()
    }

    private fun confirmarCambioEstado(
        lugar: LugarFirebase
    ) {
        val accion = if (lugar.activo) {
            "desactivar"
        } else {
            "activar"
        }

        val titulo = if (lugar.activo) {
            "Desactivar lugar"
        } else {
            "Activar lugar"
        }

        val textoBoton = if (lugar.activo) {
            "Desactivar"
        } else {
            "Activar"
        }

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

        val btnConfirmar = dialogView.findViewById<Button>(
            R.id.btnEliminarDialog
        )

        tvTitulo.text = titulo

        tvMensaje.text =
            "¿Querés $accion \"${lugar.nombre}\"?"

        btnConfirmar.text = textoBoton

        btnCancelar.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmar.setOnClickListener {
            viewModel.cambiarEstado(lugar)
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
        mostrarCargando(false)
        _binding = null
    }
}