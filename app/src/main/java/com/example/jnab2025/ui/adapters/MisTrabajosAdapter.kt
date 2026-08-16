package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.TrabajoConEstado
import java.time.format.DateTimeFormatter

class MisTrabajosAdapter(
    private val onClick: (TrabajoConEstado) -> Unit,
    private val onPagoClick: () -> Unit
) : ListAdapter<TrabajoConEstado, MisTrabajosAdapter.ItemViewHolder>(Diff()) {

    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trabajo, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val estado: TextView = itemView.findViewById(R.id.tvEstado)
        private val simposio: TextView = itemView.findViewById(R.id.tvSimposio)
        private val archivo: TextView = itemView.findViewById(R.id.tvArchivo)
        private val programacion: TextView = itemView.findViewById(R.id.tvProgramacion)
        private val motivo: TextView = itemView.findViewById(R.id.tvMotivo)
        private val pago: TextView = itemView.findViewById(R.id.tvPago)

        fun bind(trabajo: TrabajoConEstado) {
            val contexto = itemView.context

            titulo.text = trabajo.titulo
            simposio.text = trabajo.simposio
            archivo.text = "Archivo: ${trabajo.nombreArchivo}"

            estado.text = etiqueta(trabajo.estado)
            estado.setTextColor(ContextCompat.getColor(contexto, colorDe(trabajo.estado)))

            // Solo tiene dia, hora y aula si el organizador ya la programo.
            val fecha = trabajo.fecha
            val hora = trabajo.horaInicio
            if (fecha != null && hora != null) {
                programacion.visibility = View.VISIBLE
                programacion.text = buildString {
                    append("Expones el ${fecha.format(formatoFecha)} a las $hora")
                    trabajo.aula?.let { append(" en $it") }
                }
            } else {
                programacion.visibility = View.GONE
            }

            val motivoRechazo = trabajo.motivoRechazo
            if (trabajo.estado == EstadoTrabajo.RECHAZADO && !motivoRechazo.isNullOrBlank()) {
                motivo.visibility = View.VISIBLE
                motivo.text = "Motivo: $motivoRechazo"
            } else {
                motivo.visibility = View.GONE
            }

            // El pago es de la persona, no del trabajo: el aviso es el mismo
            // en todos sus trabajos.
            when {
                trabajo.inscripcionPagada -> pago.visibility = View.GONE

                trabajo.comprobanteEnviado -> {
                    pago.visibility = View.VISIBLE
                    pago.text = "Comprobante enviado, esperando verificacion"
                }

                else -> {
                    pago.visibility = View.VISIBLE
                    pago.text = "Tu inscripcion figura impaga. Toca para pagarla"
                }
            }
            pago.setOnClickListener { onPagoClick() }

            itemView.setOnClickListener { onClick(trabajo) }
        }

        private fun etiqueta(estado: EstadoTrabajo) = when (estado) {
            EstadoTrabajo.ENVIADO -> "Enviado"
            EstadoTrabajo.EN_EVALUACION -> "En evaluacion"
            EstadoTrabajo.APROBADO -> "Aprobado"
            EstadoTrabajo.RECHAZADO -> "Rechazado"
        }

        private fun colorDe(estado: EstadoTrabajo) = when (estado) {
            EstadoTrabajo.APROBADO -> R.color.green
            EstadoTrabajo.RECHAZADO -> R.color.dark_red
            else -> R.color.dusty_rose
        }
    }

    private class Diff : DiffUtil.ItemCallback<TrabajoConEstado>() {
        override fun areItemsTheSame(oldItem: TrabajoConEstado, newItem: TrabajoConEstado) =
            oldItem.trabajoId == newItem.trabajoId

        override fun areContentsTheSame(oldItem: TrabajoConEstado, newItem: TrabajoConEstado) =
            oldItem == newItem
    }
}
