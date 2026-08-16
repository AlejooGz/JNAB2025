package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.data.model.SimposioConAula
import com.example.jnab2025.databinding.ItemSimposioAdminBinding
import java.time.format.DateTimeFormatter

class MisSimposiosAdapter(
    private val onEditarClick: (SimposioConAula) -> Unit,
    private val onVerPropuestasClick: (SimposioConAula) -> Unit
) : ListAdapter<SimposioConAula, MisSimposiosAdapter.ViewHolder>(Diff()) {

    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    class ViewHolder(val binding: ItemSimposioAdminBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSimposioAdminBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val simposio = getItem(position)

        with(holder.binding) {
            tvTituloSimposioAdmin.text = simposio.titulo

            val desde = simposio.fechaInicio.format(formato)
            val hasta = simposio.fechaFin.format(formato)
            val fechas = if (desde == hasta) desde else "$desde al $hasta"
            tvDuracionAdmin.text = "$fechas  ·  ${simposio.aula}"

            btnEditar.setOnClickListener { onEditarClick(simposio) }
            btnVerPropuestas.setOnClickListener { onVerPropuestasClick(simposio) }
        }
    }

    private class Diff : DiffUtil.ItemCallback<SimposioConAula>() {
        override fun areItemsTheSame(oldItem: SimposioConAula, newItem: SimposioConAula) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SimposioConAula, newItem: SimposioConAula) =
            oldItem == newItem
    }
}
