package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.data.model.Usuario

class InscriptosAdapter : ListAdapter<Usuario, InscriptosAdapter.ViewHolder>(Diff()) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombre: TextView = view.findViewById(R.id.tvNombre)
        val detalle: TextView = view.findViewById(R.id.tvRol)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inscripto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val usuario = getItem(position)
        holder.nombre.text = usuario.nombreCompleto
        holder.detalle.text = usuario.email
    }

    private class Diff : DiffUtil.ItemCallback<Usuario>() {
        override fun areItemsTheSame(oldItem: Usuario, newItem: Usuario) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Usuario, newItem: Usuario) = oldItem == newItem
    }
}
