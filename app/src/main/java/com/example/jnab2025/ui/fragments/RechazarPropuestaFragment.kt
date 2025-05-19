package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.jnab2025.databinding.FragmentRechazarPropuestaBinding
import com.example.jnab2025.models.Propuesta

class RechazarPropuestaFragment : Fragment() {

    private var _binding: FragmentRechazarPropuestaBinding? = null
    private val binding get() = _binding!!

    private val args: RechazarPropuestaFragmentArgs by navArgs()
    private var propuestaId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRechazarPropuestaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        propuestaId = args.propuestaId

        // Obtener información de la propuesta
        val propuesta = obtenerPropuestaEjemplo(propuestaId)
        binding.tvPropuestaARechazar.text = propuesta.titulo

        // Configurar botones
        binding.btnConfirmarRechazo.setOnClickListener {
            rechazarPropuesta()
        }

        binding.btnCancelarRechazo.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun obtenerPropuestaEjemplo(id: Int): Propuesta {
        // Simulación - Esto debería venir de tu ViewModel o repositorio
        return Propuesta(
            id = id,
            simposioId = 1,
            titulo = "Propuesta ejemplo $id",
            expositor = "Dr. Ejemplo",
            descripcion = "Descripción de ejemplo",
            estado = com.example.jnab2025.models.EstadoPropuesta.PENDIENTE
        )
    }

    private fun rechazarPropuesta() {
        val mensaje = binding.etMensajeRechazo.text.toString().trim()

        if (mensaje.isEmpty()) {
            binding.tilMensajeRechazo.error = "Debe proporcionar un mensaje"
            return
        }

        // Aquí deberías actualizar el estado de la propuesta y guardar el mensaje
        // propuestaViewModel.rechazarPropuesta(propuestaId, mensaje)

        Toast.makeText(requireContext(), "Propuesta rechazada con mensaje", Toast.LENGTH_SHORT).show()

        // Navegar de regreso a la lista de propuestas
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}