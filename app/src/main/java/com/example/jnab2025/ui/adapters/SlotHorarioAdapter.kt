package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import java.time.LocalTime

class SlotHorarioAdapter<S : Any>(
    private val obtenerInicio: (S) -> LocalTime,
    private val obtenerFin: (S) -> LocalTime,
    private val estaLibre: (S) -> Boolean,
    private val obtenerOcupadoPor: (S) -> String?,
    private val onSlotClick: (S) -> Unit
) : ListAdapter<S, SlotHorarioAdapter<S>.SlotViewHolder>(
    DiffCallback<S>(obtenerInicio)
) {

    private var seleccionado: LocalTime? = null

    fun seleccionar(inicio: LocalTime?) {
        val anterior = seleccionado
        seleccionado = inicio

        currentList.forEachIndexed { indice, slot ->
            val hora = obtenerInicio(slot)

            if (hora == anterior || hora == inicio) {
                notifyItemChanged(indice)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SlotViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slot_horario, parent, false)

        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SlotViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class SlotViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val card: CardView =
            itemView.findViewById(R.id.cardSlot)

        private val horario: TextView =
            itemView.findViewById(R.id.tvHorario)

        private val estado: TextView =
            itemView.findViewById(R.id.tvEstadoSlot)

        fun bind(slot: S) {

            val contexto = itemView.context

            val inicio = obtenerInicio(slot)
            val fin = obtenerFin(slot)
            val libre = estaLibre(slot)
            val ocupadoPor = obtenerOcupadoPor(slot)

            horario.text = "$inicio - $fin"

            val esteSeleccionado =
                libre && inicio == seleccionado

            when {
                !libre -> {
                    estado.text =
                        if (ocupadoPor.isNullOrBlank()) {
                            "No disponible"
                        } else {
                            "Ocupado · $ocupadoPor"
                        }

                    card.setCardBackgroundColor(
                        ContextCompat.getColor(
                            contexto,
                            R.color.dusty_rose
                        )
                    )

                    estado.setTextColor(
                        ContextCompat.getColor(
                            contexto,
                            R.color.light_pink
                        )
                    )
                }

                esteSeleccionado -> {
                    estado.text = "Elegido"

                    card.setCardBackgroundColor(
                        ContextCompat.getColor(
                            contexto,
                            R.color.green
                        )
                    )

                    estado.setTextColor(
                        ContextCompat.getColor(
                            contexto,
                            android.R.color.white
                        )
                    )
                }

                else -> {
                    estado.text = "Libre"

                    card.setCardBackgroundColor(
                        ContextCompat.getColor(
                            contexto,
                            R.color.light_pink
                        )
                    )

                    estado.setTextColor(
                        ContextCompat.getColor(
                            contexto,
                            R.color.green
                        )
                    )
                }
            }

            horario.setTextColor(
                ContextCompat.getColor(
                    contexto,
                    if (esteSeleccionado) {
                        android.R.color.white
                    } else {
                        R.color.dark_red
                    }
                )
            )

            itemView.isEnabled = libre
            itemView.alpha = if (libre) 1f else 0.6f

            itemView.setOnClickListener {
                if (libre) {
                    onSlotClick(slot)
                }
            }
        }
    }

    private class DiffCallback<S : Any>(
        private val obtenerInicio: (S) -> LocalTime
    ) : DiffUtil.ItemCallback<S>() {

        override fun areItemsTheSame(
            oldItem: S,
            newItem: S
        ): Boolean {
            return obtenerInicio(oldItem) == obtenerInicio(newItem)
        }

        override fun areContentsTheSame(
            oldItem: S,
            newItem: S
        ): Boolean {
            return obtenerInicio(oldItem) == obtenerInicio(newItem)
        }
    }
}