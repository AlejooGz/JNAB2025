package com.example.jnab2025.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.FragmentPropuestasBinding
import com.example.jnab2025.models.Charla
import com.example.jnab2025.models.EstadoPropuesta
// import com.example.jnab2025.models.Propuesta
import com.example.jnab2025.models.Simposio
import com.example.jnab2025.models.User
import com.example.jnab2025.ui.viewmodels.CharlaViewModel
import com.example.jnab2025.ui.viewmodels.UserViewModel
import com.example.jnab2025.ui.adapters.CharlaOrganizadorAdapter

class PropuestasFragment : Fragment() {

    private var _binding: FragmentPropuestasBinding? = null
    private val binding get() = _binding!!

    private val args: PropuestasFragmentArgs by navArgs()
    private var simposioId: Int = 0

    private lateinit var adapter: CharlaOrganizadorAdapter
    private val listaPropuestas = mutableListOf<Charla>()

    private val userViewModel: UserViewModel by viewModels()
    private val charlaViewModel: CharlaViewModel by viewModels()


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

        // Pedir las charlas pendientes para este simposio
        charlaViewModel.cargarCharlasPendientesPorSimposio(simposioId)

        charlaViewModel.charlas.observe(viewLifecycleOwner) { propuestasFiltradas ->
            listaPropuestas.clear()
            listaPropuestas.addAll(propuestasFiltradas)
            adapter.notifyDataSetChanged()
            mostrarOcultarMensajeSiListaVacia()
        }

        // Obtener información del simposio
        // val simposio = charlaViewModel
        // binding.tvTituloSimposioActual.text = simposio.titulo

        // Configurar RecyclerView
        binding.rvPropuestas.layoutManager = LinearLayoutManager(requireContext())
        // cargarPropuestasEjemplo()

        adapter = CharlaOrganizadorAdapter(
            charlasPendientes = listaPropuestas,
            usuarios =  listOf(),
            onAceptarClick = { propuesta ->
                val action = PropuestasFragmentDirections.actionPropuestasFragmentToAceptarPropuestaFragment(propuesta.id)
                findNavController().navigate(action)
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

    private fun mostrarOcultarMensajeSiListaVacia() {
        if (listaPropuestas.isEmpty()) {
            binding.tvNoPropuestas.visibility = View.VISIBLE
            binding.rvPropuestas.visibility = View.GONE
        } else {
            binding.tvNoPropuestas.visibility = View.GONE
            binding.rvPropuestas.visibility = View.VISIBLE
        }
    }

    private fun cargarPropuestasEjemplo() {
        // Datos de ejemplo - Estos serían reemplazados por datos reales
    }

    private fun aceptarPropuesta(propuesta: Charla) {
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