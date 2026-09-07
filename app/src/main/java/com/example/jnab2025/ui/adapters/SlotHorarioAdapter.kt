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
import com.example.jnab2025.ui.viewmodels.CharlaViewModel
import java.time.LocalTime

/**
 * Lista de bloques horarios del dia. Los ocupados se muestran igual que los
 * libres pero no se pueden tocar, asi el organizador ve de un vistazo como
 * quedo armado el dia en esa aula.
 */
class SlotHorarioAdapter(
    private val onSlotClick: (CharlaViewModel.Slot) -> Unit
) : ListAdapter<CharlaViewModel.Slot, SlotHorarioAdapter.SlotViewHolder>(Diff()) {

    private var seleccionado: LocalTime? = null

    fun seleccionar(inicio: LocalTime?) {
        val anterior = seleccionado
        seleccionado = inicio
        currentList.forEachIndexed { indice, slot ->
            if (slot.inicio == anterior || slot.inicio == inicio) {
                notifyItemChanged(indice)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slot_horario, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: CardView = itemView.findViewById(R.id.cardSlot)
        private val horario: TextView = itemView.findViewById(R.id.tvHorario)
        private val estado: TextView = itemView.findViewById(R.id.tvEstadoSlot)

        fun bind(slot: CharlaViewModel.Slot) {
            val contexto = itemView.context
            horario.text = "${slot.inicio} - ${slot.fin}"

            val esteSeleccionado = slot.libre && slot.inicio == seleccionado

            when {
                !slot.libre -> {
                    estado.text = "Ocupado · ${slot.ocupadoPor}"
                    estado.setTextColor(ContextCompat.getColor(contexto, R.color.dusty_rose))
                    card.setCardBackgroundColor(
                        ContextCompat.getColor(contexto, R.color.light_pink)
                    )
                }

                esteSeleccionado -> {
                    estado.text = "Elegido"
                    estado.setTextColor(ContextCompat.getColor(contexto, R.color.white))
                    card.setCardBackgroundColor(
                        ContextCompat.getColor(contexto, R.color.green)
                    )
                }

                else -> {
                    estado.text = "Libre"
                    estado.setTextColor(ContextCompat.getColor(contexto, R.color.green))
                    card.setCardBackgroundColor(
                        ContextCompat.getColor(contexto, R.color.white)
                    )
                }
            }

            // El horario del slot elegido se resalta en blanco sobre el verde.
            horario.setTextColor(
                ContextCompat.getColor(
                    contexto,
                    if (esteSeleccionado) R.color.white else R.color.dark_red
                )
            )

            itemView.isEnabled = slot.libre
            itemView.alpha = if (slot.libre) 1f else 0.6f
            itemView.setOnClickListener {
                if (slot.libre) onSlotClick(slot)
            }
        }
    }

    private class Diff : DiffUtil.ItemCallback<CharlaViewModel.Slot>() {
        override fun areItemsTheSame(
            oldItem: CharlaViewModel.Slot,
            newItem: CharlaViewModel.Slot
        ) = oldItem.inicio == newItem.inicio

        override fun areContentsTheSame(
            oldItem: CharlaViewModel.Slot,
            newItem: CharlaViewModel.Slot
        ) = oldItem == newItem
    }
}
