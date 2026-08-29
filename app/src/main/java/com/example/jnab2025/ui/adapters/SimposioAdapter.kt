package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.data.model.SimposioFirebase
import java.text.SimpleDateFormat
import java.util.Locale

class SimposioAdapter :
    ListAdapter<SimposioFirebase, SimposioAdapter.SimposioViewHolder>(Diff()) {
    private val formatoFecha =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    class SimposioViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        val tvDuracion: TextView =
            itemView.findViewById(R.id.tvDuracion)
        val tvTituloSimposio: TextView =
            itemView.findViewById(R.id.tvTituloSimposio)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SimposioViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_simposio,
                parent,
                false
            )
        return SimposioViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SimposioViewHolder,
        position: Int
    ) {
        val simposio = getItem(position)
        holder.tvTituloSimposio.text =
            simposio.titulo
        val desde = simposio.fechaInicio?.toDate()
        val hasta = simposio.fechaFin?.toDate()

        holder.tvDuracion.text = when {
            desde == null || hasta == null -> "Fecha no disponible"
            formatoFecha.format(desde) ==
                    formatoFecha.format(hasta) -> formatoFecha.format(desde)
            else ->
                "${formatoFecha.format(desde)} - ${formatoFecha.format(hasta)}"
        }
    }
    private class Diff :
        DiffUtil.ItemCallback<SimposioFirebase>() {
        override fun areItemsTheSame(
            oldItem: SimposioFirebase,
            newItem: SimposioFirebase
        ): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: SimposioFirebase,
            newItem: SimposioFirebase
        ): Boolean =
            oldItem.id == newItem.id &&
                    oldItem.titulo == newItem.titulo &&
                    oldItem.temaCentral == newItem.temaCentral &&
                    oldItem.descripcion == newItem.descripcion &&
                    oldItem.fechaInicio == newItem.fechaInicio &&
                    oldItem.fechaFin == newItem.fechaFin &&
                    oldItem.aulaId == newItem.aulaId &&
                    oldItem.aulaNombre == newItem.aulaNombre
    }
}