package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.databinding.ItemSimposioAdminBinding
import com.example.jnab2025.models.Simposio

class MisSimposiosAdapter(
    private val simposios: List<Simposio>,
    private val onEditarClick: (Simposio) -> Unit,
    private val onVerPropuestasClick: (Simposio) -> Unit
) : RecyclerView.Adapter<MisSimposiosAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemSimposioAdminBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSimposioAdminBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val simposio = simposios[position]

        with(holder.binding) {
            tvTituloSimposioAdmin.text = simposio.titulo
            tvDuracionAdmin.text = "${simposio.fechaInicio} - ${simposio.fechaFin}"

            btnEditar.setOnClickListener {
                onEditarClick(simposio)
            }

            btnVerPropuestas.setOnClickListener {
                onVerPropuestasClick(simposio)
            }
        }
    }

    override fun getItemCount(): Int = simposios.size
}