package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.models.Tramite

class TramiteAdapter(
    private val tramites: List<Tramite>,
    private val onItemClick: (Tramite) -> Unit,
    private val onSubirComprobanteClick: (Tramite) -> Unit
) : RecyclerView.Adapter<TramiteAdapter.TramiteViewHolder>() {

    class TramiteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo = itemView.findViewById<TextView>(R.id.tvTitulo)
        val tvFecha = itemView.findViewById<TextView>(R.id.tvFecha)
        val tvEstado = itemView.findViewById<TextView>(R.id.tvEstado)
        val tvArchivo = itemView.findViewById<TextView>(R.id.tvArchivo)
        val ivPagado = itemView.findViewById<ImageView>(R.id.ivPagado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TramiteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_tramite, parent, false)
        return TramiteViewHolder(view)
    }

    override fun onBindViewHolder(holder: TramiteViewHolder, position: Int) {
        val tramite = tramites[position]
        holder.tvTitulo.text = tramite.tituloTrabajo
        holder.tvFecha.text = "Fecha: ${tramite.fechaEnvio}"
        holder.tvEstado.text = "Estado: ${tramite.estado}"
        holder.tvArchivo.text = "Archivo: ${tramite.nombreArchivo}"
        holder.itemView.findViewById<LinearLayout>(R.id.groupPagado).visibility =
            if (tramite.pagado) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener {
            onItemClick(tramite)
        }
    }

    override fun getItemCount(): Int = tramites.size
}


