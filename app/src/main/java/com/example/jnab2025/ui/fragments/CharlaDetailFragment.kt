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
import com.example.jnab2025.databinding.FragmentCharlaDetailBinding
import com.example.jnab2025.ui.viewmodels.CharlaViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class CharlaDetailFragment : Fragment() {

    private var _binding: FragmentCharlaDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CharlaViewModel by viewModels()
    private val args: CharlaDetailFragmentArgs by navArgs()

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
            FragmentCharlaDetailBinding.inflate(
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

        observarCharla()
        binding.btnToggleFavorito.visibility = View.GONE
    }

    private fun observarCharla() {

        val charlaId = args.charlaId

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.charlas.collectLatest { charlas ->

                    val charla =
                        charlas.firstOrNull {
                            it.id == charlaId
                        }
                            ?: return@collectLatest

                    binding.tvTituloCharla.text = charla.titulo
                    binding.tvExpositor.text = "Presentación"

                    val fecha =
                        charla.fecha
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
                        if (
                            charla.horaInicio.isNotBlank() &&
                            charla.horaFin.isNotBlank()
                        ) {
                            "${charla.horaInicio} - ${charla.horaFin} " +
                                    "(${com.example.jnab2025.data.model.CharlaFirebase.MINUTOS_PRESENTACION} minutos)"
                        } else {
                            "Horario no disponible"
                        }
                    binding.tvSala.text =
                        charla.aulaId.ifBlank {
                            "Aula no disponible"
                        }
                    binding.tvDescripcion.text =
                        "Presentación correspondiente a un trabajo aprobado."
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}