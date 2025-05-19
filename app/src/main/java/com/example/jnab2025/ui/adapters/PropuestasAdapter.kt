package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.databinding.ItemPropuestaBinding
import com.example.jnab2025.models.Propuesta

class PropuestasAdapter(
    private val propuestas: List<Propuesta>,
    private val onAceptarClick: (Propuesta) -> Unit,
    private val onRechazarClick: (Propuesta) -> Unit
) : RecyclerView.Adapter<PropuestasAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPropuestaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPropuestaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val propuesta = propuestas[position]

        with(holder.binding) {
            tvTituloPropuesta.text = propuesta.titulo
            tvExpositor.text = "Expositor: ${propuesta.expositor}"
            tvDescripcionPropuesta.text = propuesta.descripcion

            btnAceptarPropuesta.setOnClickListener {
                onAceptarClick(propuesta)
            }

            btnRechazarPropuesta.setOnClickListener {
                onRechazarClick(propuesta)
            }
        }
    }

    override fun getItemCount(): Int = propuestas.size
}