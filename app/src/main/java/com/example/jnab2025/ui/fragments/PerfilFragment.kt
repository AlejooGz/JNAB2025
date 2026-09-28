package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.jnab2025.R
import com.example.jnab2025.data.model.ComprobanteFirebase
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.InscripcionFirebase
import com.example.jnab2025.databinding.FragmentPerfilBinding
import com.example.jnab2025.ui.viewmodels.InscripcionViewModel
import com.example.jnab2025.utils.Sesion
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!
    private val inscripcionViewModel: InscripcionViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentPerfilBinding.inflate(
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

        if (!Sesion.haySesion(requireContext())) {
            Toast.makeText(
                requireContext(),
                "Debes iniciar sesión",
                Toast.LENGTH_SHORT
            ).show()

            requireActivity()
                .onBackPressedDispatcher
                .onBackPressed()

            return
        }

        mostrarDatosPersonales()
        observarInscripcion()
    }

    private fun mostrarDatosPersonales() {
        val nombre = Sesion.nombre(requireContext())
        val email = Sesion.email(requireContext()).orEmpty()

        val roles = Sesion.roles(requireContext())
            .joinToString(" / ") { rol ->
                rol.name
                    .lowercase()
                    .replaceFirstChar { letra ->
                        letra.uppercase()
                    }
            }

        binding.tvNombrePrincipal.text = nombre
        /** binding.tvEmailPrincipal.text = email */

        binding.tvNombreCompleto.text = nombre
        binding.tvEmail.text = email
        binding.tvRol.text = roles.ifBlank {
            "Usuario"
        }

        binding.tvIniciales.text =
            obtenerIniciales(nombre)
    }

    private fun obtenerIniciales(
        nombreCompleto: String
    ): String {

        val partes = nombreCompleto
            .trim()
            .split(Regex("\\s+"))
            .filter {
                it.isNotBlank()
            }

        return when {
            partes.isEmpty() -> "U"

            partes.size == 1 ->
                partes.first()
                    .take(2)
                    .uppercase()

            else ->
                "${partes.first().first()}" +
                        "${partes.last().first()}"
        }.uppercase()
    }

    private fun observarInscripcion() {
        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                inscripcionViewModel.vista.collectLatest { vista ->

                    pintarEstadoInscripcion(
                        inscripcion = vista.inscripcion,
                        comprobante = vista.comprobante
                    )
                }
            }
        }
    }

    private fun pintarEstadoInscripcion(
        inscripcion: InscripcionFirebase?,
        comprobante: ComprobanteFirebase?
    ) {
        if (inscripcion == null) {
            mostrarEstado(
                titulo = "Todavía no estás inscripto",
                detalle = "No registraste una inscripción para las jornadas.",
                etiqueta = "SIN INSCRIPCIÓN",
                fondo = R.drawable.bg_estado_neutro,
                colorTexto = R.color.dark_red
            )
            return
        }

        if (
            inscripcion.estado ==
            EstadoInscripcion.ANULADA.name
        ) {
            mostrarEstado(
                titulo = "Inscripción anulada",
                detalle = "Contactate con la organización para obtener más información.",
                etiqueta = "ANULADA",
                fondo = R.drawable.bg_estado_rechazado,
                colorTexto = R.color.dark_red
            )
            return
        }

        if (
            inscripcion.estado ==
            EstadoInscripcion.PAGADA.name
        ) {
            mostrarEstado(
                titulo = "Inscripción confirmada",
                detalle = construirDetalleConfirmado(
                    inscripcion
                ),
                etiqueta = "ACREDITADA",
                fondo = R.drawable.bg_estado_aprobado,
                colorTexto = android.R.color.holo_green_dark
            )
            return
        }

        when (comprobante?.estado) {

            EstadoComprobante.PENDIENTE.name -> {
                mostrarEstado(
                    titulo = "Comprobante en revisión",
                    detalle =
                        "La organización todavía debe verificar tu comprobante.",
                    etiqueta = "PENDIENTE",
                    fondo = R.drawable.bg_estado_pendiente,
                    colorTexto = R.color.dark_red
                )
            }

            EstadoComprobante.VERIFICADO.name -> {
                mostrarEstado(
                    titulo = "Comprobante verificado",
                    detalle =
                        "Tu pago fue verificado por la organización.",
                    etiqueta = "VERIFICADO",
                    fondo = R.drawable.bg_estado_aprobado,
                    colorTexto = android.R.color.holo_green_dark
                )
            }

            EstadoComprobante.RECHAZADO.name -> {
                mostrarEstado(
                    titulo = "Comprobante rechazado",
                    detalle =
                        comprobante.motivoRechazo
                            ?.takeIf { it.isNotBlank() }
                            ?: "Debés cargar un nuevo comprobante de pago.",
                    etiqueta = "RECHAZADO",
                    fondo = R.drawable.bg_estado_rechazado,
                    colorTexto = R.color.dark_red
                )
            }

            else -> {
                mostrarEstado(
                    titulo = "Inscripción pendiente de pago",
                    detalle =
                        "Tu inscripción está registrada. Falta cargar el comprobante.",
                    etiqueta = "PENDIENTE DE PAGO",
                    fondo = R.drawable.bg_estado_pendiente,
                    colorTexto = R.color.dark_red
                )
            }
        }
    }

    private fun construirDetalleConfirmado(
        inscripcion: InscripcionFirebase
    ): String {

        val tipo = formatearValor(
            inscripcion.tipo
        )

        val categoria = formatearValor(
            inscripcion.categoria
        )

        return "$tipo · $categoria. No tenés nada pendiente."
    }

    private fun formatearValor(
        valor: String
    ): String {

        return valor
            .lowercase()
            .replace("_", " ")
            .replaceFirstChar { letra ->
                letra.uppercase()
            }
    }

    private fun mostrarEstado(
        titulo: String,
        detalle: String,
        etiqueta: String,
        fondo: Int,
        colorTexto: Int
    ) {
        binding.tvTituloInscripcion.text = titulo
        binding.tvDetalleInscripcion.text = detalle
        binding.tvEstadoInscripcion.text = etiqueta

        binding.tvEstadoInscripcion.setBackgroundResource(
            fondo
        )

        binding.tvEstadoInscripcion.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                colorTexto
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}