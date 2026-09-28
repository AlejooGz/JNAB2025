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
import com.example.jnab2025.data.model.TrabajoSeguimientoFirebase
import java.time.format.DateTimeFormatter

class MisTrabajosAdapter(
    private val onClick: (TrabajoSeguimientoFirebase) -> Unit,
    private val onPagoClick: () -> Unit
) : ListAdapter<
        TrabajoSeguimientoFirebase,
        MisTrabajosAdapter.ItemViewHolder
        >(Diff()) {

    private val formatoFecha =
        DateTimeFormatter.ofPattern("dd/MM")

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_trabajo,
                parent,
                false
            )

        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ItemViewHolder,
        position: Int
    ) {

        holder.bind(
            getItem(position)
        )
    }

    inner class ItemViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val titulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val estado: TextView = itemView.findViewById(R.id.tvEstado)

        private val simposio: TextView = itemView.findViewById(R.id.tvSimposio)
        private val archivo: TextView = itemView.findViewById(R.id.tvArchivo)
        private val programacion: TextView = itemView.findViewById(R.id.tvProgramacion)
        private val motivo: TextView = itemView.findViewById(R.id.tvMotivo)
        private val pago: TextView = itemView.findViewById(R.id.tvPago)

        fun bind(
            seguimiento: TrabajoSeguimientoFirebase
        ) {
            val trabajo = seguimiento.trabajo
            val contexto = itemView.context
            titulo.text = trabajo.titulo
            simposio.text = trabajo.simposioTitulo
            archivo.text = "Archivo: ${trabajo.nombreArchivo}"
            estado.text = etiqueta(trabajo.estado)
            estado.setTextColor(
                ContextCompat.getColor(
                    contexto,
                    colorDe(trabajo.estado)
                )
            )
            mostrarProgramacion(seguimiento)
            mostrarMotivo(seguimiento)

            pago.visibility = View.GONE

            itemView.setOnClickListener {
                onClick(seguimiento)
            }
        }

        private fun mostrarProgramacion(
            seguimiento: TrabajoSeguimientoFirebase
        ) {
            val trabajo = seguimiento.trabajo
            val fecha = seguimiento.fecha
            val desde = seguimiento.horaInicio
            val hasta = seguimiento.horaFin

            if (
                trabajo.estado == "APROBADO" &&
                fecha != null &&
                desde != null
            ) {
                programacion.visibility = View.VISIBLE

                programacion.text =
                    buildString {
                        append(
                            "Exponés el ${
                                fecha.format(
                                    formatoFecha
                                )
                            }"
                        )
                        append(
                            " de $desde"
                        )
                        if (hasta != null) {
                            append(
                                " a $hasta"
                            )
                        }
                        seguimiento.aula?.let { aula ->
                            append(
                                " en $aula"
                            )
                        }
                    }
            } else {
                programacion.visibility = View.GONE
            }
        }

        private fun mostrarMotivo(
            seguimiento: TrabajoSeguimientoFirebase
        ) {
            val trabajo = seguimiento.trabajo
            val motivoRechazo = trabajo.motivoRechazo

            if (
                trabajo.estado == "RECHAZADO" &&
                !motivoRechazo.isNullOrBlank()
            ) {
                motivo.visibility = View.VISIBLE
                motivo.text =
                    "Motivo: $motivoRechazo"

            } else {
                motivo.visibility = View.GONE
            }
        }

        private fun etiqueta(
            estado: String
        ): String = when (estado) {
            "ENVIADO" -> "Enviado"
            "EN_EVALUACION" -> "En evaluación"
            "APROBADO" -> "Aprobado"
            "RECHAZADO" -> "Rechazado"
            else -> estado
        }

        private fun colorDe(
            estado: String
        ): Int = when (estado) {
            "APROBADO" -> R.color.green
            "RECHAZADO" -> R.color.dark_red
            else -> R.color.dusty_rose
        }
    }

    private class Diff :
        DiffUtil.ItemCallback<TrabajoSeguimientoFirebase>() {

        override fun areItemsTheSame(
            oldItem: TrabajoSeguimientoFirebase,
            newItem: TrabajoSeguimientoFirebase
        ): Boolean {

            return oldItem.trabajo.id ==
                    newItem.trabajo.id
        }

        override fun areContentsTheSame(
            oldItem: TrabajoSeguimientoFirebase,
            newItem: TrabajoSeguimientoFirebase
        ): Boolean {

            return oldItem.trabajo ==
                    newItem.trabajo &&
                    oldItem.fecha ==
                    newItem.fecha &&
                    oldItem.horaInicio ==
                    newItem.horaInicio &&
                    oldItem.horaFin ==
                    newItem.horaFin &&
                    oldItem.aula ==
                    newItem.aula
        }
    }
}