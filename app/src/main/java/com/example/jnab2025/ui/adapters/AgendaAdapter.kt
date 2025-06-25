package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.models.AgendaCharlaEntity

class AgendaAdapter :
    ListAdapter<AgendaCharlaEntity, AgendaAdapter.CharlaViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharlaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mi_agenda, parent, false)
        return CharlaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CharlaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CharlaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.tvTituloCharla)
        private val hora: TextView = itemView.findViewById(R.id.tvHoraCharla)
        private val rol: TextView = itemView.findViewById(R.id.tvRolCharla)

        fun bind(charla: AgendaCharlaEntity) {
            titulo.text = charla.titulo
            hora.text = charla.hora
            rol.text = if (charla.esExpositor) "Expositor" else "Asistente"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<AgendaCharlaEntity>() {
        override fun areItemsTheSame(oldItem: AgendaCharlaEntity, newItem: AgendaCharlaEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AgendaCharlaEntity, newItem: AgendaCharlaEntity): Boolean {
            return oldItem == newItem
        }
    }
}
