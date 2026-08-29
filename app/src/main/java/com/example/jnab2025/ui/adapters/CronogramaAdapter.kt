package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.data.model.ItemAgendaFirebase
import com.example.jnab2025.data.model.TipoActividad

class CronogramaAdapter(
    private val onAgendaClick: (ItemAgendaFirebase) -> Unit,
    private val onItemClick: (ItemAgendaFirebase) -> Unit
) : ListAdapter<ItemAgendaFirebase, CronogramaAdapter.ItemViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cronograma, parent, false)
        return ItemViewHolder(view)
    }
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val horaInicio: TextView = itemView.findViewById(R.id.tvHoraInicio)
        private val horaFin: TextView = itemView.findViewById(R.id.tvHoraFin)
        private val titulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val detalle: TextView = itemView.findViewById(R.id.tvDetalle)
        private val simposio: TextView = itemView.findViewById(R.id.tvSimposio)
        private val aula: TextView = itemView.findViewById(R.id.tvAula)
        private val agenda: ImageButton = itemView.findViewById(R.id.ibAgenda)

        fun bind(item: ItemAgendaFirebase) {
            horaInicio.text = item.horaInicio.toString()
            horaFin.text = item.horaFin.toString()
            titulo.text = item.titulo
            detalle.text = item.expositor ?: etiqueta(item.tipo)
            simposio.text = item.simposio.orEmpty()
            simposio.visibility = if (item.simposio.isNullOrBlank()) View.GONE else View.VISIBLE
            aula.text = ubicacion(item)
            aula.visibility = if (item.aula == null) View.GONE else View.VISIBLE

            agenda.setImageResource(
                if (item.enMiAgenda) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
            agenda.contentDescription =
                if (item.enMiAgenda) "Quitar de mi agenda" else "Agregar a mi agenda"

            agenda.setOnClickListener { onAgendaClick(item) }
            itemView.setOnClickListener { onItemClick(item) }
        }

        private fun ubicacion(item: ItemAgendaFirebase): String {
            val nombre = item.aula ?: return ""
            val piso = item.piso ?: return nombre
            return if (piso == 0) "$nombre - planta baja" else "$nombre - piso $piso"
        }
        private fun etiqueta(tipo: TipoActividad) = when (tipo) {
            TipoActividad.COFFEE_BREAK -> "Coffee break"
            TipoActividad.ACREDITACION -> "Acreditacion"
            TipoActividad.CONFERENCIA -> "Conferencia"
            TipoActividad.PRESENTACION -> "Presentacion"
            TipoActividad.OTRO -> "Actividad"
        }
    }
    private class Diff : DiffUtil.ItemCallback<ItemAgendaFirebase>() {
        override fun areItemsTheSame(oldItem: ItemAgendaFirebase, newItem: ItemAgendaFirebase) =
            oldItem.charlaId == newItem.charlaId

        override fun areContentsTheSame(oldItem: ItemAgendaFirebase, newItem: ItemAgendaFirebase) =
            oldItem == newItem
    }
}
