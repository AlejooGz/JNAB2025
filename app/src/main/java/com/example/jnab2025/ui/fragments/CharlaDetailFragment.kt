package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.jnab2025.databinding.FragmentCharlaDetailBinding
import com.example.jnab2025.ui.viewmodels.CharlaViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class CharlaDetailFragment : Fragment() {

    private var _binding: FragmentCharlaDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CharlaViewModel by viewModels()
    private val args: CharlaDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharlaDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aquí obtendrías la charla por ID desde el ViewModel
        // Por ahora, asumiendo que pasaste el ID como argumento
        val charlaId = args.charlaId

        // Cargar los detalles de la charla
        cargarDetallesCharla(charlaId)
    }

    private fun cargarDetallesCharla(charlaId: Int) {
        // Aquí implementarías la lógica para mostrar los detalles
        // Necesitarías agregar un método en el ViewModel para obtener una charla por ID
    }

    private fun formatearFecha(fechaStr: String): String {
        return try {
            val formatoOriginal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoDestino = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
            val fecha = formatoOriginal.parse(fechaStr)
            fecha?.let { formatoDestino.format(it) } ?: fechaStr
        } catch (e: Exception) {
            fechaStr
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}