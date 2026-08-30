package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.databinding.ItemSimposioAdminBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MisSimposiosAdapter(
    private val onEditarClick: (SimposioFirebase) -> Unit,
    private val onVerPropuestasClick: (SimposioFirebase) -> Unit,
    private val onVerTrabajosClick: (SimposioFirebase) -> Unit
) : ListAdapter<SimposioFirebase, MisSimposiosAdapter.ViewHolder>(Diff()) {

    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    class ViewHolder(
        val binding: ItemSimposioAdminBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemSimposioAdminBinding.inflate(
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

        val simposio = getItem(position)

        with(holder.binding) {

            tvTituloSimposioAdmin.text = simposio.titulo

            val desde = simposio.fechaInicio
                ?.toDate()
                ?.toInstant()
                ?.atZone(ZoneId.systemDefault())
                ?.toLocalDate()
                ?.format(formato)
                ?: "Sin fecha"

            val hasta = simposio.fechaFin
                ?.toDate()
                ?.toInstant()
                ?.atZone(ZoneId.systemDefault())
                ?.toLocalDate()
                ?.format(formato)
                ?: "Sin fecha"

            val fechas =
                if (desde == hasta) {
                    desde
                } else {
                    "$desde al $hasta"
                }

            tvDuracionAdmin.text =
                "$fechas  ·  ${simposio.aulaNombre}"

            btnEditar.setOnClickListener {
                onEditarClick(simposio)
            }

            btnVerPropuestas.setOnClickListener {
                onVerPropuestasClick(simposio)
            }

            btnVerTrabajos.setOnClickListener {
                onVerTrabajosClick(simposio)
            }
        }
    }

    private class Diff :
        DiffUtil.ItemCallback<SimposioFirebase>() {

        override fun areItemsTheSame(
            oldItem: SimposioFirebase,
            newItem: SimposioFirebase
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: SimposioFirebase,
            newItem: SimposioFirebase
        ): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.organizadorUid == newItem.organizadorUid &&
                    oldItem.aulaId == newItem.aulaId &&
                    oldItem.aulaNombre == newItem.aulaNombre &&
                    oldItem.aulaEdificio == newItem.aulaEdificio &&
                    oldItem.aulaPiso == newItem.aulaPiso &&
                    oldItem.titulo == newItem.titulo &&
                    oldItem.descripcion == newItem.descripcion &&
                    oldItem.temaCentral == newItem.temaCentral &&
                    oldItem.fechaInicio == newItem.fechaInicio &&
                    oldItem.fechaFin == newItem.fechaFin
        }
    }
}