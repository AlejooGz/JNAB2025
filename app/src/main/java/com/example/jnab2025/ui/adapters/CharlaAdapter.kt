package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.models.Charla
import java.text.SimpleDateFormat
import java.util.Locale

class CharlaAdapter(
    private var charlas: List<Charla>,
    private val onFavoritoClick: (Charla) -> Unit
) : RecyclerView.Adapter<CharlaAdapter.CharlaViewHolder>() {

    class CharlaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvTituloCharla: TextView = itemView.findViewById(R.id.tvTituloCharla)
        val tvExpositor: TextView = itemView.findViewById(R.id.tvExpositor)
        val tvHorario: TextView = itemView.findViewById(R.id.tvHorario)
        val tvSala: TextView = itemView.findViewById(R.id.tvSala)
        val ivDestacado: ImageView = itemView.findViewById(R.id.ivDestacado)
        val ibFavorito: ImageButton = itemView.findViewById(R.id.ibFavorito)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharlaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_charla, parent, false)
        return CharlaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharlaViewHolder, position: Int) {
        val charla = charlas[position]


        val fechaFormateada = formatearFecha(charla.fecha)

        holder.tvFecha.text = fechaFormateada
        holder.tvTituloCharla.text = charla.titulo
        holder.tvExpositor.text = charla.expositor
        holder.tvHorario.text = "${charla.horaInicio} - ${charla.horaFin}"
        holder.tvSala.text = charla.sala

        // Mostrar icono de destacado si es una charla fundamental
        holder.ivDestacado.visibility = if (charla.esDestacado) View.VISIBLE else View.GONE

        // Configurar el estado del botón favorito
        holder.ibFavorito.setImageResource(
            if (charla.esFavorito) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )

        // Configurar el listener para el botón de favorito
        holder.ibFavorito.setOnClickListener {
            onFavoritoClick(charla)
        }
    }

    override fun getItemCount(): Int = charlas.size


    fun actualizarCharlas(nuevasCharlas: List<Charla>) {
        charlas = nuevasCharlas
        notifyDataSetChanged()
    }


    private fun formatearFecha(fechaStr: String): String {
        try {
            val formatoOriginal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatoDestino = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val fecha = formatoOriginal.parse(fechaStr)
            return fecha?.let { formatoDestino.format(it) } ?: fechaStr
        } catch (e: Exception) {
            return fechaStr
        }
    }
}