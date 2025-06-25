package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.models.Simposio

class SimposioTramiteAdapter(
    private val simposios: List<Simposio>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<SimposioTramiteAdapter.SimposioViewHolder>() {

    inner class SimposioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDuracion: TextView = itemView.findViewById(R.id.tvDuracion)
        private val tvTituloSimposio: TextView = itemView.findViewById(R.id.tvTituloSimposio)

        fun bind(simposio: Simposio) {
            tvTituloSimposio.text = simposio.titulo
            tvDuracion.text = "${simposio.fechaInicio} - ${simposio.fechaFin}"
            itemView.setOnClickListener {
                onItemClick(simposio.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimposioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simposio, parent, false)
        return SimposioViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimposioViewHolder, position: Int) {
        holder.bind(simposios[position])
    }

    override fun getItemCount(): Int = simposios.size
}
