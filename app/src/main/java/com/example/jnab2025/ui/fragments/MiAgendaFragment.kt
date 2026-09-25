package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.data.model.ItemAgendaFirebase
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.databinding.FragmentMiAgendaBinding
import com.example.jnab2025.ui.adapters.MiAgendaAdapter
import com.example.jnab2025.ui.viewmodels.CronogramaViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * La agenda del usuario, dia por dia. La tienen asistentes y expositores.
 *
 * Es distinta del cronograma: el cronograma es todo lo que pasa en el congreso
 * y sirve para elegir; esta pantalla es el recorrido del dia ya armado, con la
 * hora y el aula de cada cosa. Arriba se resume en una linea, en la forma en
 * que lo pide el enunciado: "14:00 en Sala 1, 14:40 en Sala 8 y 16:00 el
 * coffee break".
 */
class MiAgendaFragment : Fragment() {

    private var _binding: FragmentMiAgendaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CronogramaViewModel by viewModels()
    private lateinit var adapter: MiAgendaAdapter

    /** Evita que seleccionar la pestania por codigo dispare el listener. */
    private var actualizandoTabs = false

    private val formatoTab = DateTimeFormatter.ofPattern("EEE d/MM", Locale("es", "AR"))
    private val formatoDia = DateTimeFormatter.ofPattern("EEEE d/MM", Locale("es", "AR"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMiAgendaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarLista()
        configurarTabs()
        observar()
    }

    private fun configurarLista() {
        adapter = MiAgendaAdapter(
            onItemClick = { item ->
                val accion = MiAgendaFragmentDirections
                    .actionMiAgendaFragmentToCharlaDetailFragment(item.charlaId)
                findNavController().navigate(accion)
            },
            /* La estrella solo quita. alternarAgenda con una charla que esta en
             * la agenda la borra, y al borrarla la fila desaparece de la lista:
             * desde aca no se puede volver a agregar, y esta bien que sea asi. */
            onQuitarClick = { item -> viewModel.alternarAgenda(item) }
        )
        binding.rvAgenda.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAgenda.adapter = adapter
    }

    private fun configurarTabs() {
        binding.tabsDias.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (actualizandoTabs) return
                (tab.tag as? LocalDate)?.let { viewModel.seleccionarDia(it) }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) = Unit
            override fun onTabReselected(tab: TabLayout.Tab) = Unit
        })
    }

    private fun observar() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.dias.collectLatest { dias -> pintarTabs(dias) }
                }

                launch {
                    viewModel.itinerario.collectLatest { filas ->
                        adapter.submitList(filas)
                        pintarEncabezado(filas)

                        val vacia = filas.isEmpty()
                        binding.rvAgenda.visibility =
                            if (vacia) View.GONE else View.VISIBLE
                        binding.tvVacio.visibility =
                            if (vacia) View.VISIBLE else View.GONE
                        binding.tvVacio.text =
                            if (viewModel.dias.value.isEmpty()) {
                                "Todavía no hay actividades publicadas."
                            } else {
                                "No tenés nada agendado para este día. " +
                                        "Marcá con la estrella las charlas que " +
                                        "te interesen desde Charlas."
                            }
                    }
                }

                launch {
                    viewModel.avisos.collectLatest { aviso ->
                        Toast.makeText(requireContext(), aviso, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    /**
     * El encabezado: el dia, la linea del enunciado y el aviso de problemas.
     *
     * La linea se corta en las primeras cuatro actividades; con mas deja de
     * leerse de un golpe, que es justamente para lo que sirve.
     */
    private fun pintarEncabezado(filas: List<CronogramaViewModel.FilaAgenda>) {
        val actividades = filas
            .filterIsInstance<CronogramaViewModel.FilaAgenda.Actividad>()
            .map { it.item }

        val tramos = filas.filterIsInstance<CronogramaViewModel.FilaAgenda.Tramo>()
        val dia = viewModel.dia.value

        binding.tvDia.text = when {
            dia == null -> "Sin día seleccionado"
            else -> {
                val elegidas = actividades.count { it.enMiAgenda }
                val fecha = dia.format(formatoDia).replaceFirstChar { it.uppercase() }
                when (elegidas) {
                    0 -> fecha
                    1 -> "$fecha · 1 charla elegida"
                    else -> "$fecha · $elegidas charlas elegidas"
                }
            }
        }

        if (actividades.isEmpty()) {
            binding.tvResumen.text = "Sin actividades."
            binding.tvAviso.visibility = View.GONE
            return
        }

        val partes = actividades
            .take(4)
            .map { item -> "${item.horaInicio} ${destino(item)}" }
        val resumen = partes.joinToString(" · ")

        binding.tvResumen.text =
            if (actividades.size > partes.size) "$resumen · …" else resumen

        // Un solo aviso arriba, para no tener que recorrer la lista buscandolo.
        val superpuestos = tramos.count { it.seSuperpone }
        val ajustados = tramos.count { it.ajustado }

        val aviso = when {
            superpuestos == 1 -> "Ojo: dos actividades se superponen."
            superpuestos > 1 -> "Ojo: hay $superpuestos superposiciones en el día."
            ajustados == 1 -> "Ojo: en un tramo tenés que cambiar de sala con poco tiempo."
            ajustados > 1 -> "Ojo: en $ajustados tramos tenés que moverte con poco tiempo."
            else -> null
        }

        binding.tvAviso.text = aviso.orEmpty()
        binding.tvAviso.visibility = if (aviso == null) View.GONE else View.VISIBLE
    }

    /** "en Sala 1" para una charla, "coffee break" para lo que no tiene aula. */
    private fun destino(item: ItemAgendaFirebase): String {
        val aula = item.aula
        return when {
            item.tipo == TipoActividad.COFFEE_BREAK -> "coffee break"
            aula.isNullOrBlank() -> MiAgendaAdapter.etiquetaDe(item.tipo).lowercase()
            else -> "en $aula"
        }
    }

    private fun pintarTabs(dias: List<LocalDate>) {
        if (dias.isEmpty()) return

        val yaEstan = (0 until binding.tabsDias.tabCount)
            .mapNotNull { binding.tabsDias.getTabAt(it)?.tag as? LocalDate }
        if (yaEstan == dias) return

        actualizandoTabs = true
        binding.tabsDias.removeAllTabs()
        dias.forEach { dia ->
            val tab = binding.tabsDias.newTab()
                .setText(dia.format(formatoTab).replaceFirstChar { it.uppercase() })
            tab.tag = dia
            binding.tabsDias.addTab(tab, dia == viewModel.dia.value)
        }
        actualizandoTabs = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvAgenda.adapter = null
        _binding = null
    }
}
