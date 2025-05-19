package com.example.jnab2025.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jnab2025.databinding.FragmentEditarSimposioBinding
import com.example.jnab2025.models.Simposio
import java.text.SimpleDateFormat
import java.util.*

class EditarSimposioFragment : Fragment() {

    private var _binding: FragmentEditarSimposioBinding? = null
    private val binding get() = _binding!!

    private val args: EditarSimposioFragmentArgs by navArgs()
    private var simposioId: Int = 0

    private var fechaInicio: String = ""
    private var fechaFin: String = ""
    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditarSimposioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        simposioId = args.simposioId

        // Cargar datos del simposio
        cargarDatosSimposio()

        // Configurar los selectores de fecha
        binding.btnFechaInicioEditar.setOnClickListener {
            mostrarSelectorFecha(true)
        }

        binding.btnFechaFinEditar.setOnClickListener {
            mostrarSelectorFecha(false)
        }

        // Configurar el botón para guardar cambios
        binding.btnGuardarCambiosSimposio.setOnClickListener {
            guardarCambiosSimposio()
        }
    }

    private fun cargarDatosSimposio() {
        // Aquí deberías obtener los datos del simposio desde tu ViewModel o Repository
        // Simulamos esto con datos de ejemplo
        val simposio = obtenerSimposioEjemplo(simposioId)

        binding.etTituloSimposioEditar.setText(simposio.titulo)
        // Supongamos que tenemos descripción en algún lugar
        binding.etDescripcionSimposioEditar.setText("Descripción del simposio")

        fechaInicio = simposio.fechaInicio
        fechaFin = simposio.fechaFin

        binding.tvFechaInicioSeleccionadaEditar.text = fechaInicio
        binding.tvFechaFinSeleccionadaEditar.text = fechaFin
    }

    private fun obtenerSimposioEjemplo(id: Int): Simposio {
        // Simulación - Esto debería venir de tu ViewModel o repositorio
        return Simposio(
            id = id,
            titulo = "Simposio ejemplo $id",
            fechaInicio = "2025-05-15",
            fechaFin = "2025-05-17"
        )
    }

    private fun mostrarSelectorFecha(esInicio: Boolean) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                val fechaFormateada = formatoFecha.format(calendar.time)

                if (esInicio) {
                    fechaInicio = fechaFormateada
                    binding.tvFechaInicioSeleccionadaEditar.text = fechaFormateada
                } else {
                    fechaFin = fechaFormateada
                    binding.tvFechaFinSeleccionadaEditar.text = fechaFormateada
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun guardarCambiosSimposio() {
        val titulo = binding.etTituloSimposioEditar.text.toString().trim()
        val descripcion = binding.etDescripcionSimposioEditar.text.toString().trim()

        // Validar campos
        if (titulo.isEmpty()) {
            binding.tilTituloSimposioEditar.error = "El título es obligatorio"
            return
        }

        if (descripcion.isEmpty()) {
            binding.tilDescripcionSimposioEditar.error = "La descripción es obligatoria"
            return
        }

        if (fechaInicio.isEmpty()) {
            Toast.makeText(requireContext(), "Seleccione una fecha de inicio", Toast.LENGTH_SHORT).show()
            return
        }

        if (fechaFin.isEmpty()) {
            Toast.makeText(requireContext(), "Seleccione una fecha de fin", Toast.LENGTH_SHORT).show()
            return
        }

        // Actualizar el objeto simposio (usando ViewModel o Repository)
        val simposioActualizado = Simposio(
            id = simposioId,
            titulo = titulo,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )

        // Aquí deberías guardar los cambios en tu base de datos o repositorio
        // simposioViewModel.actualizarSimposio(simposioActualizado)

        Toast.makeText(requireContext(), "Cambios guardados con éxito", Toast.LENGTH_SHORT).show()

        // Navegar de regreso
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}