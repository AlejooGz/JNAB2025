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
import com.example.jnab2025.data.model.FaqFirebase
import com.example.jnab2025.databinding.FragmentFaqBinding
import com.example.jnab2025.models.FaqItem
import com.example.jnab2025.ui.adapters.FaqAdapter
import com.example.jnab2025.ui.viewmodels.FaqViewModel
import kotlinx.coroutines.launch

class FAQFragment : Fragment() {
    private var _binding: FragmentFaqBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FaqViewModel by viewModels()
    private lateinit var adapter: FaqAdapter
    private val items = mutableListOf<FaqItem>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaqBinding.inflate(
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
        configurarRecycler()
        observarFaqs()
        observarAvisos()
    }
    private fun configurarRecycler() {
        adapter = FaqAdapter(items)
        binding.recyclerFaq.layoutManager =
            LinearLayoutManager(requireContext())
        binding.recyclerFaq.adapter = adapter
    }

    private fun observarFaqs() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.faqs.collect { faqsFirebase ->
                    actualizarListado(faqsFirebase)
                }
            }
        }
    }
    private fun actualizarListado(
        faqs: List<FaqFirebase>
    ) {
        items.clear()
        val asistentes =
            faqs
                .filter {
                    it.publico == "ASISTENTE"
                }
                .sortedBy {
                    it.orden
                }
        val expositores =
            faqs
                .filter { it.publico == "EXPOSITOR" }
                .sortedBy { it.orden }
        if (asistentes.isNotEmpty()) {
            items.add(
                FaqItem(
                    question = "Asistentes",
                    isHeader = true
                )
            )
            asistentes.forEach { faq ->
                items.add(faq.toFaqItem())
            }
        }
        if (expositores.isNotEmpty()) {
            items.add(
                FaqItem(
                    question = "Expositores",
                    isHeader = true
                )
            )
            expositores.forEach { faq ->
                items.add(
                    faq.toFaqItem()
                )
            }
        }
        adapter.notifyDataSetChanged()
    }

    private fun FaqFirebase.toFaqItem(): FaqItem {
        return FaqItem(
            question = pregunta,
            answer = respuesta,
            isExpanded = false,
            isHeader = false
        )
    }

    private fun observarAvisos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.avisos.collect { mensaje ->
                    Toast.makeText(
                        requireContext(),
                        mensaje,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}