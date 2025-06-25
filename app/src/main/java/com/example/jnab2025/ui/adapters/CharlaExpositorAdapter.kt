package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.models.Charla
import com.example.jnab2025.models.EstadoPropuesta

class CharlaExpositorAdapter(
    private val charlas: List<Charla>,
    private val onItemClick: (Charla) -> Unit,
    private val onSubirComprobanteClick: (Charla) -> Unit
) : RecyclerView.Adapter<CharlaExpositorAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTituloCharla)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstado)
        val tvArchivo: TextView = itemView.findViewById(R.id.tvArchivo)
        val btnSubirComprobante: Button = itemView.findViewById(R.id.btnSubirComprobante)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_charla_expositor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val charla = charlas[position]

        holder.tvTitulo.text = charla.titulo
        holder.tvEstado.text = "Estado: ${charla.estado}"
        holder.tvArchivo.text = "Archivo: ${charla.nombreArchivo}"

        // Mostrar botón solo si está aprobada y no pagada
        if (charla.estado == EstadoPropuesta.APROBADA && !charla.pagado) {
            holder.btnSubirComprobante.visibility = View.VISIBLE
        } else {
            holder.btnSubirComprobante.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(charla)
        }

        holder.btnSubirComprobante.setOnClickListener {
            onSubirComprobanteClick(charla)
        }
    }

    override fun getItemCount(): Int = charlas.size
}
