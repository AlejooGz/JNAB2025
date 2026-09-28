package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.data.model.SimposioFirebase
import java.text.SimpleDateFormat
import java.util.Locale

class SimposioAdapter :
    ListAdapter<
            SimposioFirebase,
            SimposioAdapter.SimposioViewHolder
            >(Diff()) {

    private val formatoFecha =
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.getDefault()
        )

    private val formatoFechaHora =
        SimpleDateFormat(
            "dd/MM/yyyy HH:mm",
            Locale.getDefault()
        )

    private var simposioExpandidoId: String? = null

    class SimposioViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        val contenedorResumen: LinearLayout =
            itemView.findViewById(
                R.id.contenedorResumen
            )

        val contenedorDetalle: LinearLayout =
            itemView.findViewById(
                R.id.contenedorDetalle
            )

        val tvTituloSimposio: TextView =
            itemView.findViewById(
                R.id.tvTituloSimposio
            )

        val tvDuracion: TextView =
            itemView.findViewById(
                R.id.tvDuracion
            )

        val tvAccionDetalle: TextView =
            itemView.findViewById(
                R.id.tvAccionDetalle
            )

        val tvIndicador: TextView =
            itemView.findViewById(
                R.id.tvIndicador
            )

        val tvTemaCentral: TextView =
            itemView.findViewById(
                R.id.tvTemaCentral
            )

        val tvDescripcion: TextView =
            itemView.findViewById(
                R.id.tvDescripcion
            )

        val tvFechaHorario: TextView =
            itemView.findViewById(
                R.id.tvFechaHorario
            )

        val tvUbicacion: TextView =
            itemView.findViewById(
                R.id.tvUbicacion
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SimposioViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_simposio,
                parent,
                false
            )

        return SimposioViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SimposioViewHolder,
        position: Int
    ) {
        val simposio = getItem(position)

        holder.tvTituloSimposio.text =
            simposio.titulo.ifBlank {
                "Simposio sin título"
            }

        holder.tvDuracion.text =
            construirDuracion(simposio)

        holder.tvTemaCentral.text =
            simposio.temaCentral.ifBlank {
                "Tema no especificado"
            }

        holder.tvDescripcion.text =
            simposio.descripcion.ifBlank {
                "No se proporcionó una descripción."
            }

        holder.tvFechaHorario.text =
            construirFechaHorario(simposio)

        holder.tvUbicacion.text =
            construirUbicacion(simposio)

        val estaExpandido =
            simposio.id == simposioExpandidoId

        pintarExpansion(
            holder = holder,
            estaExpandido = estaExpandido
        )
        holder.contenedorResumen.setOnClickListener {
            alternarDetalle(simposio)
        }
        holder.tvTituloSimposio.setOnClickListener {
            alternarDetalle(simposio)
        }
    }

    private fun alternarDetalle(
        simposio: SimposioFirebase
    ) {
        val idAnterior = simposioExpandidoId

        simposioExpandidoId =
            if (idAnterior == simposio.id) {
                null
            } else {
                simposio.id
            }

        if (idAnterior != null) {
            val posicionAnterior =
                currentList.indexOfFirst {
                    it.id == idAnterior
                }

            if (posicionAnterior != -1) {
                notifyItemChanged(
                    posicionAnterior
                )
            }
        }

        val posicionNueva =
            currentList.indexOfFirst {
                it.id == simposio.id
            }

        if (posicionNueva != -1) {
            notifyItemChanged(
                posicionNueva
            )
        }
    }

    private fun pintarExpansion(
        holder: SimposioViewHolder,
        estaExpandido: Boolean
    ) {
        holder.contenedorDetalle.visibility =
            if (estaExpandido) {
                View.VISIBLE
            } else {
                View.GONE
            }

        holder.tvIndicador.text =
            if (estaExpandido) {
                "▲"
            } else {
                "▼"
            }

        holder.tvAccionDetalle.text =
            if (estaExpandido) {
                "Ocultar detalles"
            } else {
                "Ver detalles"
            }

        holder.contenedorResumen.contentDescription =
            if (estaExpandido) {
                "Ocultar detalles del simposio"
            } else {
                "Ver detalles del simposio"
            }
    }

    private fun construirDuracion(
        simposio: SimposioFirebase
    ): String {
        val desde =
            simposio.fechaInicio?.toDate()

        val hasta =
            simposio.fechaFin?.toDate()

        return when {
            desde == null || hasta == null ->
                "Fecha no disponible"

            formatoFecha.format(desde) ==
                    formatoFecha.format(hasta) ->
                formatoFecha.format(desde)

            else ->
                "${formatoFecha.format(desde)} - " +
                        formatoFecha.format(hasta)
        }
    }

    private fun construirFechaHorario(
        simposio: SimposioFirebase
    ): String {
        val desde =
            simposio.fechaInicio?.toDate()

        val hasta =
            simposio.fechaFin?.toDate()

        return when {
            desde == null || hasta == null ->
                "Fecha y horario no disponibles"

            else ->
                "${formatoFechaHora.format(desde)} - " +
                        formatoFechaHora.format(hasta)
        }
    }

    private fun construirUbicacion(
        simposio: SimposioFirebase
    ): String {
        val partes = mutableListOf<String>()

        if (simposio.aulaNombre.isNotBlank()) {
            partes.add(simposio.aulaNombre)
        }

        if (simposio.aulaEdificio.isNotBlank()) {
            partes.add(simposio.aulaEdificio)
        }
        partes.add(
            if (simposio.aulaPiso == 0) {
                "Planta baja"
            } else {
                "Piso ${simposio.aulaPiso}"
            }
        )

        return partes
            .takeIf { it.isNotEmpty() }
            ?.joinToString(" · ")
            ?: "Ubicación no disponible"
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
            return oldItem.id ==
                    newItem.id &&
                    oldItem.titulo ==
                    newItem.titulo &&
                    oldItem.temaCentral ==
                    newItem.temaCentral &&
                    oldItem.descripcion ==
                    newItem.descripcion &&
                    oldItem.fechaInicio ==
                    newItem.fechaInicio &&
                    oldItem.fechaFin ==
                    newItem.fechaFin &&
                    oldItem.aulaId ==
                    newItem.aulaId &&
                    oldItem.aulaNombre ==
                    newItem.aulaNombre &&
                    oldItem.aulaEdificio ==
                    newItem.aulaEdificio &&
                    oldItem.aulaPiso ==
                    newItem.aulaPiso
        }
    }
}