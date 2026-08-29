package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.data.model.Simposio
import java.time.format.DateTimeFormatter

class SimposioAdapter(
    private var simposios: List<Simposio>
) : RecyclerView.Adapter<SimposioAdapter.SimposioViewHolder>() {

    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    class SimposioViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val tvDuracion: TextView =
            itemView.findViewById(R.id.tvDuracion)

        val tvTituloSimposio: TextView =
            itemView.findViewById(R.id.tvTituloSimposio)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SimposioViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simposio, parent, false)

        return SimposioViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SimposioViewHolder,
        position: Int
    ) {
        val simposio = simposios[position]

        holder.tvTituloSimposio.text = simposio.titulo

        val desde = simposio.fechaInicio.format(formato)
        val hasta = simposio.fechaFin.format(formato)

        holder.tvDuracion.text =
            if (desde == hasta) {
                desde
            } else {
                "$desde al $hasta"
            }
    }

    override fun getItemCount(): Int = simposios.size

    fun actualizarLista(nuevaLista: List<Simposio>) {
        simposios = nuevaLista
        notifyDataSetChanged()
    }
}