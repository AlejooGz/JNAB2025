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

class SimposioPickerAdapter(
    private val onClick: (SimposioFirebase) -> Unit
) : ListAdapter<SimposioFirebase, SimposioPickerAdapter.ItemViewHolder>(Diff()) {

    private val formato =
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_simposio_picker,
                parent,
                false
            )
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val titulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val tema: TextView = itemView.findViewById(R.id.tvTema)
        private val fechas: TextView = itemView.findViewById(R.id.tvFechas)
        private val aula: TextView = itemView.findViewById(R.id.tvAula)

        fun bind(simposio: SimposioFirebase) {
            titulo.text = simposio.titulo
            tema.text = simposio.temaCentral
            val desde = simposio.fechaInicio?.toDate()
            val hasta = simposio.fechaFin?.toDate()
            fechas.text = when {
                desde == null || hasta == null ->
                    "Fecha no disponible"
                formato.format(desde) ==
                        formato.format(hasta) ->
                    formato.format(desde)
                else ->
                    "${formato.format(desde)} al ${formato.format(hasta)}"
            }
            aula.text = simposio.aulaNombre
            itemView.setOnClickListener {
                onClick(simposio)
            }
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
                    oldItem.fechaInicio == newItem.fechaInicio &&
                    oldItem.fechaFin == newItem.fechaFin &&
                    oldItem.aulaId == newItem.aulaId &&
                    oldItem.aulaNombre == newItem.aulaNombre
    }
}