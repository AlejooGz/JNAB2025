package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.databinding.FragmentActividadDetailBinding
import com.example.jnab2025.ui.viewmodels.ActividadViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ActividadDetailFragment : Fragment() {

    private var _binding: FragmentActividadDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ActividadViewModel by viewModels()
    private val args: ActividadDetailFragmentArgs by navArgs()

    private val formatoFecha =
        DateTimeFormatter.ofPattern(
            "EEEE, dd 'de' MMMM 'de' yyyy",
            Locale("es", "AR")
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentActividadDetailBinding.inflate(
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
        super.onViewCreated(view,
            savedInstanceState
        )

        observarActividad()

        viewModel.cargarActividad(args.actividadId
        )
    }

    private fun observarActividad() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.actividad.collectLatest { actividad ->
                    if (actividad == null) {
                        return@collectLatest
                    }

                    binding.tvTituloActividad.text = actividad.titulo
                    val tipo =
                        runCatching {
                            TipoActividad.valueOf(
                                actividad.tipo
                            )
                        }.getOrDefault(
                            TipoActividad.OTRO
                        )

                    binding.tvTipoActividad.text = etiquetaTipo(tipo)
                    val fecha =
                        actividad.fecha
                            ?.toDate()
                            ?.toInstant()
                            ?.atZone(
                                ZoneId.systemDefault()
                            )
                            ?.toLocalDate()
                    binding.tvFecha.text =
                        fecha
                            ?.format(formatoFecha)
                            ?: "Fecha no disponible"
                    binding.tvHorario.text =
                        construirHorario(
                            actividad.horaInicio,
                            actividad.horaFin
                        )
                    binding.tvSala.text =
                        construirUbicacion(
                            aulaNombre = actividad.aulaNombre,
                            edificio = actividad.aulaEdificio,
                            piso = actividad.aulaPiso
                        )
                    binding.tvDescripcion.text =
                        actividad.descripcion
                            .ifBlank {
                                "Sin descripción disponible."
                            }
                }
            }
        }
    }

    private fun construirHorario(
        horaInicio: String,
        horaFin: String
    ): String {
        if (horaInicio.isBlank() || horaFin.isBlank()
        ) {
            return "Horario no disponible"
        }

        val inicio =
            runCatching {
                LocalTime.parse(horaInicio)
            }.getOrNull()

        val fin =
            runCatching {
                LocalTime.parse(horaFin)
            }.getOrNull()

        if (
            inicio == null || fin == null
        ) {
            return "$horaInicio - $horaFin"
        }

        val minutos =
            Duration
                .between(
                    inicio,
                    fin
                )
                .toMinutes()
        return "$horaInicio - $horaFin ($minutos minutos)"
    }

    private fun construirUbicacion(
        aulaNombre: String,
        edificio: String,
        piso: Int
    ): String {
        if (aulaNombre.isBlank()) {
            return "Actividad general · sin aula"
        }

        val partes = mutableListOf<String>()

        partes += aulaNombre

        if (edificio.isNotBlank()) {
            partes += edificio
        }

        if (piso > 0) {
            partes += "Piso $piso"
        }
        return partes.joinToString(" - ")
    }

    private fun etiquetaTipo(
        tipo: TipoActividad
    ): String {
        return when (tipo) {
            TipoActividad.PRESENTACION -> "Presentación"
            TipoActividad.CONFERENCIA -> "Conferencia"
            TipoActividad.COFFEE_BREAK -> "Coffee break"
            TipoActividad.ACREDITACION -> "Acreditación"
            TipoActividad.OTRO -> "Otra actividad"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}