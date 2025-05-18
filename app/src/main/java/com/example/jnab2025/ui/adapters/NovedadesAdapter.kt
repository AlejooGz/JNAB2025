package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.models.Novedad

class NovedadesAdapter(private val novedadesList: List<Novedad>) :
    RecyclerView.Adapter<NovedadesAdapter.NovedadViewHolder>() {

    class NovedadViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImagenNovedad: ImageView = itemView.findViewById(R.id.ivImagenNovedad)
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvDescripcion)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NovedadViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_novedad, parent, false)
        return NovedadViewHolder(view)
    }

    override fun onBindViewHolder(holder: NovedadViewHolder, position: Int) {
        val novedad = novedadesList[position]
        holder.ivImagenNovedad.setImageResource(novedad.imagenResId)
        holder.tvTitulo.text = novedad.titulo
        holder.tvDescripcion.text = novedad.descripcion
        holder.tvFecha.text = novedad.fecha
    }

    override fun getItemCount(): Int {
        return novedadesList.size
    }
}