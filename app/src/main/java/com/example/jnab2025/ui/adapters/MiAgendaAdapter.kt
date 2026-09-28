package com.example.jnab2025.ui.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.data.model.ItemAgendaFirebase
import com.example.jnab2025.data.model.TipoActividad
import com.example.jnab2025.ui.viewmodels.CronogramaViewModel.FilaAgenda

/**
 * El itinerario del dia: actividad, tramo, actividad, tramo.
 *
 * El cronograma es una lista plana de tarjetas para elegir con la estrella.
 * Esto es otra cosa: una linea de tiempo donde entre dos actividades se ve el
 * traslado, que es la "ruta organizada" que pide el enunciado.
 */
class MiAgendaAdapter(
    private val onItemClick: (ItemAgendaFirebase) -> Unit,
    private val onQuitarClick: (ItemAgendaFirebase) -> Unit
) : ListAdapter<FilaAgenda, RecyclerView.ViewHolder>(Diff()) {

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is FilaAgenda.Actividad -> TIPO_ACTIVIDAD
            is FilaAgenda.Tramo -> TIPO_TRAMO
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflador = LayoutInflater.from(parent.context)
        return if (viewType == TIPO_TRAMO) {
            TramoViewHolder(
                inflador.inflate(R.layout.item_agenda_tramo, parent, false)
            )
        } else {
            ActividadViewHolder(
                inflador.inflate(R.layout.item_mi_agenda, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val fila = getItem(position)) {
            is FilaAgenda.Actividad ->
                (holder as ActividadViewHolder).bind(fila.item, position)

            is FilaAgenda.Tramo ->
                (holder as TramoViewHolder).bind(fila)
        }
    }

    inner class ActividadViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val punto: View = itemView.findViewById(R.id.punto)
        private val lineaArriba: View = itemView.findViewById(R.id.lineaArriba)
        private val lineaAbajo: View = itemView.findViewById(R.id.lineaAbajo)
        private val hora: TextView = itemView.findViewById(R.id.tvHora)
        private val horaFin: TextView = itemView.findViewById(R.id.tvHoraFin)
        private val etiqueta: TextView = itemView.findViewById(R.id.tvEtiqueta)
        private val estrella: ImageView = itemView.findViewById(R.id.ivEstrella)
        private val titulo: TextView = itemView.findViewById(R.id.tvTitulo)
        private val lugar: TextView = itemView.findViewById(R.id.tvLugar)
        private val detalle: TextView = itemView.findViewById(R.id.tvDetalle)

        fun bind(item: ItemAgendaFirebase, position: Int) {
            hora.text = item.horaInicio.toString()
            horaFin.text = "a ${item.horaFin}"
            titulo.text = item.titulo.ifBlank { etiquetaDe(item.tipo) }

            /* Lo que el usuario eligio se marca con la misma estrella del
             * cronograma, asi se reconoce sin leer. El resto lleva la etiqueta
             * del tipo, que es la informacion que falta en esos casos.
             *
             * Aca la estrella solo sirve para quitar. Volver a agregar no tiene
             * sentido: al sacarla, la actividad se va de esta lista, asi que no
             * queda nada que volver a marcar. Para agregar esta el cronograma. */
            if (item.enMiAgenda) {
                estrella.visibility = View.VISIBLE
                etiqueta.visibility = View.GONE
                estrella.setOnClickListener { onQuitarClick(item) }
            } else {
                estrella.visibility = View.GONE
                estrella.setOnClickListener(null)
                etiqueta.visibility = View.VISIBLE
                etiqueta.text = etiquetaDe(item.tipo).uppercase()
            }

            /* Las actividades generales del congreso se marcan distinto: el
             * usuario no las eligio, estan porque son de todos. */
            val color = if (item.enMiAgenda) R.color.dark_red else R.color.dusty_rose
            punto.backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, color)
            )

            // La linea no arranca antes de la primera ni sigue despues de la ultima.
            lineaArriba.visibility =
                if (position == 0) View.INVISIBLE else View.VISIBLE
            lineaAbajo.visibility =
                if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE

            val donde = ubicacion(item)
            lugar.text = donde
            lugar.visibility = if (donde.isBlank()) View.GONE else View.VISIBLE

            val extra = item.expositor
                ?.let { "Expositor: $it" }
                ?: item.simposio
            detalle.text = extra.orEmpty()
            detalle.visibility = if (extra.isNullOrBlank()) View.GONE else View.VISIBLE

            itemView.setOnClickListener { onItemClick(item) }
        }

        private fun ubicacion(item: ItemAgendaFirebase): String {
            val nombre = item.aula ?: return ""
            val piso = item.piso ?: return nombre
            return if (piso == 0) "$nombre · planta baja" else "$nombre · piso $piso"
        }
    }

    inner class TramoViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val espera: TextView = itemView.findViewById(R.id.tvEspera)
        private val ruta: TextView = itemView.findViewById(R.id.tvRuta)

        fun bind(tramo: FilaAgenda.Tramo) {
            val contexto = itemView.context

            espera.text = when {
                tramo.seSuperpone -> "Se superponen"
                tramo.minutosLibres == 0L -> "Una sigue a la otra, sin corte"
                else -> "${duracion(tramo.minutosLibres)} libres"
            }

            /* Rojo cuando hay un problema real: que se pisen, o que haya que
             * cruzar el edificio con diez minutos o menos. */
            val alerta = tramo.seSuperpone || tramo.ajustado
            espera.setTextColor(
                ContextCompat.getColor(
                    contexto,
                    if (alerta) R.color.dark_red else R.color.dusty_rose
                )
            )

            ruta.text = textoRuta(tramo)
        }

        private fun textoRuta(tramo: FilaAgenda.Tramo): String {
            val hasta = tramo.hastaAula

            return when {
                tramo.seSuperpone ->
                    "Vas a tener que elegir una de las dos."

                hasta.isNullOrBlank() ->
                    "Sin sala asignada todavía."

                tramo.mismoLugar ->
                    "Te quedás en $hasta, no te movés."

                tramo.cambiaDePiso -> {
                    val desde = tramo.desdeAula ?: "la anterior"
                    val subeOBaja =
                        if ((tramo.hastaPiso ?: 0) > (tramo.desdePiso ?: 0)) "subís" else "bajás"
                    "De $desde a $hasta: $subeOBaja de ${piso(tramo.desdePiso)} " +
                            "a ${piso(tramo.hastaPiso)}."
                }

                else -> {
                    val desde = tramo.desdeAula ?: "la anterior"
                    "De $desde a $hasta, mismo piso."
                }
            }
        }

        private fun piso(piso: Int?): String = when (piso) {
            null -> "otro piso"
            0 -> "planta baja"
            else -> "piso $piso"
        }

        private fun duracion(minutos: Long): String = when {
            minutos < 60 -> "$minutos minutos"
            minutos % 60 == 0L -> "${minutos / 60} h"
            else -> "${minutos / 60} h ${minutos % 60} min"
        }
    }

    private class Diff : DiffUtil.ItemCallback<FilaAgenda>() {
        override fun areItemsTheSame(oldItem: FilaAgenda, newItem: FilaAgenda) =
            oldItem.clave == newItem.clave

        override fun areContentsTheSame(oldItem: FilaAgenda, newItem: FilaAgenda) =
            oldItem == newItem
    }

    companion object {
        private const val TIPO_ACTIVIDAD = 0
        private const val TIPO_TRAMO = 1

        fun etiquetaDe(tipo: TipoActividad) = when (tipo) {
            TipoActividad.COFFEE_BREAK -> "Coffee break"
            TipoActividad.ACREDITACION -> "Acreditación"
            TipoActividad.CONFERENCIA -> "Conferencia"
            TipoActividad.PRESENTACION -> "Presentación"
            TipoActividad.OTRO -> "Actividad"
        }
    }
}
