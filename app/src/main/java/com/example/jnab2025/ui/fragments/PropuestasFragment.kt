package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.FragmentPropuestasBinding
import com.example.jnab2025.models.EstadoPropuesta
import com.example.jnab2025.models.Propuesta
import com.example.jnab2025.models.Simposio
import com.example.jnab2025.ui.adapters.PropuestasAdapter

class PropuestasFragment : Fragment() {

    private var _binding: FragmentPropuestasBinding? = null
    private val binding get() = _binding!!

    private val args: PropuestasFragmentArgs by navArgs()
    private var simposioId: Int = 0

    private lateinit var adapter: PropuestasAdapter
    private val listaPropuestas = mutableListOf<Propuesta>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPropuestasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        simposioId = args.simposioId

        // Obtener información del simposio
        val simposio = obtenerSimposioEjemplo(simposioId)
        binding.tvTituloSimposioActual.text = simposio.titulo

        // Configurar RecyclerView
        binding.rvPropuestas.layoutManager = LinearLayoutManager(requireContext())
        cargarPropuestasEjemplo()

        adapter = PropuestasAdapter(
            propuestas = listaPropuestas,
            onAceptarClick = { propuesta ->
                aceptarPropuesta(propuesta)
            },
            onRechazarClick = { propuesta ->
                // Navegar al fragment de rechazo con mensaje
                val action = PropuestasFragmentDirections.actionPropuestasFragmentToRechazarPropuestaFragment(propuesta.id)
                findNavController().navigate(action)
            }
        )

        binding.rvPropuestas.adapter = adapter

        // Mostrar mensaje si no hay propuestas
        if (listaPropuestas.isEmpty()) {
            binding.tvNoPropuestas.visibility = View.VISIBLE
            binding.rvPropuestas.visibility = View.GONE
        } else {
            binding.tvNoPropuestas.visibility = View.GONE
            binding.rvPropuestas.visibility = View.VISIBLE
        }
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

    private fun cargarPropuestasEjemplo() {
        // Datos de ejemplo - Estos serían reemplazados por datos reales
        listaPropuestas.apply {
            clear()
            add(
                Propuesta(
                    id = 1,
                    simposioId = simposioId,
                    titulo = "Avances en genética de poblaciones",
                    expositor = "Dr. Juan Pérez",
                    descripcion = "Presentación sobre los últimos avances en genética de poblaciones humanas...",
                    estado = EstadoPropuesta.PENDIENTE
                )
            )
            add(
                Propuesta(
                    id = 2,
                    simposioId = simposioId,
                    titulo = "Técnicas de análisis de ADN antiguo",
                    expositor = "Dra. María Rodríguez",
                    descripcion = "Recientes desarrollos en técnicas para analizar ADN de restos arqueológicos...",
                    estado = EstadoPropuesta.PENDIENTE
                )
            )
            add(
                Propuesta(
                    id = 3,
                    simposioId = simposioId,
                    titulo = "Aplicaciones de la inteligencia artificial en paleoantropología",
                    expositor = "Dr. Carlos Sánchez",
                    descripcion = "Uso de IA para la reconstrucción de especímenes fósiles y análisis de patrones evolutivos...",
                    estado = EstadoPropuesta.PENDIENTE
                )
            )
        }
    }

    private fun aceptarPropuesta(propuesta: Propuesta) {
        // Actualizar estado de la propuesta
        // En un caso real esto se haría a través del ViewModel
        Toast.makeText(requireContext(), "Propuesta aceptada: ${propuesta.titulo}", Toast.LENGTH_SHORT).show()

        // En un caso real actualizaríamos la UI o recargaríamos los datos
        // Por simplicidad, simulamos que se ha aceptado y eliminamos del adapter
        val index = listaPropuestas.indexOfFirst { it.id == propuesta.id }
        if (index != -1) {
            listaPropuestas.removeAt(index)
            adapter.notifyItemRemoved(index)

            // Mostrar mensaje si no hay propuestas
            if (listaPropuestas.isEmpty()) {
                binding.tvNoPropuestas.visibility = View.VISIBLE
                binding.rvPropuestas.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}