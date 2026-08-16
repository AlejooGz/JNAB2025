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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.FragmentCronogramaBinding
import com.example.jnab2025.ui.adapters.CronogramaAdapter
import com.example.jnab2025.ui.viewmodels.CronogramaViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Cronograma del congreso, dia por dia. Reemplaza a la pantalla que leia de la
 * base vieja y que nunca llegaba a mostrar nada porque su observador no se
 * disparaba.
 */
class AgendaFragment : Fragment() {

    private var _binding: FragmentCronogramaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CronogramaViewModel by viewModels()
    private lateinit var adapter: CronogramaAdapter

    /** Evita que seleccionar la pestania por codigo dispare el listener. */
    private var actualizandoTabs = false

    private val formatoDia = DateTimeFormatter.ofPattern("EEE d/MM", Locale("es", "AR"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCronogramaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarLista()
        configurarTabs()
        configurarFiltro()
        observar()
    }

    private fun configurarLista() {
        adapter = CronogramaAdapter(
            onAgendaClick = { viewModel.alternarAgenda(it) },
            onItemClick = { item ->
                Toast.makeText(requireContext(), item.titulo, Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvCronograma.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCronograma.adapter = adapter
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

    private fun configurarFiltro() {
        binding.fabSoloAgenda.setOnClickListener { viewModel.alternarSoloMiAgenda() }
    }

    private fun observar() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.dias.collectLatest { dias -> pintarTabs(dias) }
                }

                launch {
                    viewModel.items.collectLatest { items ->
                        adapter.submitList(items)
                        binding.tvVacio.visibility =
                            if (items.isEmpty()) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.soloMiAgenda.collectLatest { solo ->
                        binding.fabSoloAgenda.setImageResource(
                            if (solo) android.R.drawable.btn_star_big_on
                            else android.R.drawable.btn_star_big_off
                        )
                        binding.tvTitulo.text = if (solo) "Mi agenda" else "Cronograma"
                        binding.tvVacio.text = if (solo) {
                            "Todavia no agendaste nada de este dia"
                        } else {
                            "No hay actividades para mostrar"
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

    private fun pintarTabs(dias: List<LocalDate>) {
        if (dias.isEmpty()) return

        val yaEstan = (0 until binding.tabsDias.tabCount)
            .mapNotNull { binding.tabsDias.getTabAt(it)?.tag as? LocalDate }
        if (yaEstan == dias) return

        actualizandoTabs = true
        binding.tabsDias.removeAllTabs()
        dias.forEach { dia ->
            val tab = binding.tabsDias.newTab()
                .setText(dia.format(formatoDia).replaceFirstChar { it.uppercase() })
            tab.tag = dia
            binding.tabsDias.addTab(tab, dia == viewModel.dia.value)
        }
        actualizandoTabs = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvCronograma.adapter = null
        _binding = null
    }
}
