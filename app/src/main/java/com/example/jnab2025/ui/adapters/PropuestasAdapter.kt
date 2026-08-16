package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.data.model.PropuestaPendiente
import com.example.jnab2025.databinding.ItemPropuestaBinding

class PropuestasAdapter(
    private val onVerPdfClick: (PropuestaPendiente) -> Unit,
    private val onAceptarClick: (PropuestaPendiente) -> Unit,
    private val onRechazarClick: (PropuestaPendiente) -> Unit
) : ListAdapter<PropuestaPendiente, PropuestasAdapter.ViewHolder>(Diff()) {

    class ViewHolder(val binding: ItemPropuestaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPropuestaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val propuesta = getItem(position)

        with(holder.binding) {
            tvTituloPropuesta.text = propuesta.titulo
            // El nombre sale del join con usuario: antes siempre decia "Desconocido"
            // porque el fragment le pasaba una lista de usuarios vacia.
            tvExpositor.text = buildString {
                append("Expositor: ${propuesta.autor}")
                propuesta.institucion?.let { append(" ($it)") }
            }
            tvDescripcionPropuesta.text = propuesta.resumen

            btnVerPdf.text = "Ver PDF: ${propuesta.nombreArchivo}"
            btnVerPdf.setOnClickListener { onVerPdfClick(propuesta) }

            btnAceptarPropuesta.setOnClickListener { onAceptarClick(propuesta) }
            btnRechazarPropuesta.setOnClickListener { onRechazarClick(propuesta) }
        }
    }

    private class Diff : DiffUtil.ItemCallback<PropuestaPendiente>() {
        override fun areItemsTheSame(oldItem: PropuestaPendiente, newItem: PropuestaPendiente) =
            oldItem.trabajoId == newItem.trabajoId

        override fun areContentsTheSame(oldItem: PropuestaPendiente, newItem: PropuestaPendiente) =
            oldItem == newItem
    }
}
