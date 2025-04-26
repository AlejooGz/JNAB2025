package com.example.jnab2025

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SimposioAdapter (
    private var simposios: List<Simposio>,
) : RecyclerView.Adapter<SimposioAdapter.SimposioViewHolder>() {

    class SimposioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDuracion: TextView = itemView.findViewById(R.id.tvDuracion)
        val tvTituloSimposio: TextView = itemView.findViewById(R.id.tvTituloSimposio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimposioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simposio, parent, false)
        return SimposioViewHolder(view)
    }

    override fun onBindViewHolder(holder: SimposioViewHolder, position: Int) {
        val simposio = simposios[position]

        holder.tvTituloSimposio.text = simposio.titulo
        holder.tvDuracion.text = "${simposio.fechaInicio} - ${simposio.fechaFin}"
    }

    override fun getItemCount(): Int = simposios.size

}