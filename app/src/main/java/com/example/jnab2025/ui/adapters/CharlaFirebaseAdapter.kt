package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.data.model.CharlaFirebase
import com.example.jnab2025.databinding.ItemCharlaBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter
class CharlaFirebaseAdapter(
    private val onItemClick: (CharlaFirebase) -> Unit
) : ListAdapter<
        CharlaFirebase,
        CharlaFirebaseAdapter.ViewHolder
        >(Diff()) {

    private val formatoFecha =
        DateTimeFormatter.ofPattern("dd/MM/yyyy")

    class ViewHolder(
        val binding: ItemCharlaBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemCharlaBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val charla =
            getItem(position)

        with(holder.binding) {

            val fecha =
                charla.fecha
                    ?.toDate()
                    ?.toInstant()
                    ?.atZone(
                        ZoneId.systemDefault()
                    )
                    ?.toLocalDate()

            tvFecha.text =
                fecha
                    ?.format(formatoFecha)
                    ?: "Sin fecha"

            tvTituloCharla.text =
                charla.titulo

            tvHorario.text =
                if (
                    charla.horaInicio.isNotBlank() &&
                    charla.horaFin.isNotBlank()
                ) {
                    "${charla.horaInicio} - ${charla.horaFin}"
                } else {
                    "Horario sin definir"
                }
            tvExpositor.text = "Presentación"
            tvSala.text = charla.aulaId
            ibFavorito.visibility = android.view.View.GONE
            ivDestacado.visibility = android.view.View.GONE
            root.setOnClickListener { onItemClick(charla)
            }
        }
    }

    private class Diff :
        DiffUtil.ItemCallback<CharlaFirebase>() {

        override fun areItemsTheSame(
            oldItem: CharlaFirebase,
            newItem: CharlaFirebase
        ): Boolean {

            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: CharlaFirebase,
            newItem: CharlaFirebase
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}