package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.data.model.TrabajoFirebase
import com.example.jnab2025.databinding.ItemPropuestaBinding

class PropuestasAdapter(
    private val onVerPdfClick: (TrabajoFirebase) -> Unit,
    private val onAceptarClick: (TrabajoFirebase) -> Unit,
    private val onRechazarClick: (TrabajoFirebase) -> Unit
) : ListAdapter<TrabajoFirebase, PropuestasAdapter.ViewHolder>(Diff()) {

    class ViewHolder(
        val binding: ItemPropuestaBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemPropuestaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val trabajo = getItem(position)
        with(holder.binding) {
            tvTituloPropuesta.text = trabajo.titulo
            tvExpositor.text = "Expositor: ${trabajo.autorNombre}"
            tvDescripcionPropuesta.text = trabajo.resumen
            btnVerPdf.text = "Ver PDF: ${trabajo.nombreArchivo}"
            btnVerPdf.setOnClickListener {
                onVerPdfClick(trabajo)
            }
            btnAceptarPropuesta.setOnClickListener {
                onAceptarClick(trabajo)
            }
            btnRechazarPropuesta.setOnClickListener {
                onRechazarClick(trabajo)
            }
        }
    }
    private class Diff :
        DiffUtil.ItemCallback<TrabajoFirebase>() {
        override fun areItemsTheSame(
            oldItem: TrabajoFirebase,
            newItem: TrabajoFirebase
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: TrabajoFirebase,
            newItem: TrabajoFirebase
        ): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.estado == newItem.estado &&
                    oldItem.titulo == newItem.titulo &&
                    oldItem.resumen == newItem.resumen &&
                    oldItem.archivoUrl == newItem.archivoUrl
        }
    }
}