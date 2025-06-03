package com.example.jnab2025.ui.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.jnab2025.R
import com.example.jnab2025.databinding.FragmentCrearSimposioBinding
import com.example.jnab2025.ui.viewmodels.SimposioViewModel
import com.example.jnab2025.models.Simposio
import java.text.SimpleDateFormat
import java.util.*

class CrearSimposioFragment : Fragment() {

    private var _binding: FragmentCrearSimposioBinding? = null
    private val binding get() = _binding!!

    private val simposioViewModel: SimposioViewModel by viewModels()

    private var fechaInicio: String = ""
    private var fechaFin: String = ""
    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCrearSimposioBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar los selectores de fecha
        binding.btnFechaInicio.setOnClickListener {
            mostrarSelectorFecha(true)
        }

        binding.btnFechaFin.setOnClickListener {
            mostrarSelectorFecha(false)
        }

        // Configurar el botón para crear simposio
        binding.btnCrearSimposio.setOnClickListener {
            crearSimposio()
        }
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
                    binding.tvFechaInicioSeleccionada.text = fechaFormateada
                } else {
                    fechaFin = fechaFormateada
                    binding.tvFechaFinSeleccionada.text = fechaFormateada
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun crearSimposio() {
        val titulo = binding.etTituloSimposio.text.toString().trim()
        val descripcion = binding.etDescripcionSimposio.text.toString().trim()

        // Validar campos
        if (titulo.isEmpty()) {
            binding.tilTituloSimposio.error = "El título es obligatorio"
            return
        }

        if (descripcion.isEmpty()) {
            binding.tilDescripcionSimposio.error = "La descripción es obligatoria"
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

        // Crear el objeto simposio (asumiendo que tienes un ViewModel o Repository)
        val nuevoSimposio = Simposio(
            id = 0, // ID temporal
            titulo = titulo,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )

        // Aquí deberías guardar el simposio en tu base de datos o repositorio
        // simposioViewModel.guardarSimposio(nuevoSimposio)

        simposioViewModel.insertar(nuevoSimposio)

        Toast.makeText(requireContext(), "Simposio creado con éxito", Toast.LENGTH_SHORT).show()

        // Navegar de regreso
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}