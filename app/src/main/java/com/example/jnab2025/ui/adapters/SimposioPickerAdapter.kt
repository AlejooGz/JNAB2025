package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.data.model.SimposioConAula
import java.time.format.DateTimeFormatter

class SimposioPickerAdapter(
    private val onClick: (SimposioConAula) -> Unit
) : ListAdapter<SimposioConAula, SimposioPickerAdapter.ItemViewHolder>(Diff()) {

    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simposio_picker, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val tema: TextView = itemView.findViewById(R.id.tvTema)
        private val fechas: TextView = itemView.findViewById(R.id.tvFechas)
        private val aula: TextView = itemView.findViewById(R.id.tvAula)

        fun bind(simposio: SimposioConAula) {
            titulo.text = simposio.titulo
            tema.text = simposio.temaCentral

            val desde = simposio.fechaInicio.format(formato)
            val hasta = simposio.fechaFin.format(formato)
            fechas.text = if (desde == hasta) desde else "$desde al $hasta"

            aula.text = if (simposio.piso == 0) {
                "${simposio.aula} - planta baja"
            } else {
                "${simposio.aula} - piso ${simposio.piso}"
            }

            itemView.setOnClickListener { onClick(simposio) }
        }
    }

    private class Diff : DiffUtil.ItemCallback<SimposioConAula>() {
        override fun areItemsTheSame(oldItem: SimposioConAula, newItem: SimposioConAula) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SimposioConAula, newItem: SimposioConAula) =
            oldItem == newItem
    }
}
