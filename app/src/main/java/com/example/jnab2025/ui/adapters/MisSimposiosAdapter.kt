package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.data.model.SimposioFirebase
import com.example.jnab2025.databinding.ItemEncabezadoAulaBinding
import com.example.jnab2025.databinding.ItemSimposioAdminBinding
import com.example.jnab2025.ui.viewmodels.MisSimposiosViewModel.Fila
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Los simposios del organizador, agrupados por aula.
 *
 * La lista alterna encabezados de aula y simposios. El orden lo decide el
 * ViewModel: aulas alfabeticamente y, adentro de cada una, por fecha.
 */
class MisSimposiosAdapter(
    private val onEditarClick: (SimposioFirebase) -> Unit,
    private val onVerPropuestasClick: (SimposioFirebase) -> Unit,
    private val onVerTrabajosClick: (SimposioFirebase) -> Unit
) : ListAdapter<Fila, RecyclerView.ViewHolder>(Diff()) {

    private val formato = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    class AulaViewHolder(
        val binding: ItemEncabezadoAulaBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class SimposioViewHolder(
        val binding: ItemSimposioAdminBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Fila.Aula -> TIPO_AULA
            is Fila.Simposio -> TIPO_SIMPOSIO
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        val inflador = LayoutInflater.from(parent.context)

        return if (viewType == TIPO_AULA) {
            AulaViewHolder(
                ItemEncabezadoAulaBinding.inflate(inflador, parent, false)
            )
        } else {
            SimposioViewHolder(
                ItemSimposioAdminBinding.inflate(inflador, parent, false)
            )
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val fila = getItem(position)) {
            is Fila.Aula -> pintarAula(holder as AulaViewHolder, fila)
            is Fila.Simposio -> pintarSimposio(holder as SimposioViewHolder, fila.simposio)
        }
    }

    private fun pintarAula(holder: AulaViewHolder, aula: Fila.Aula) {
        with(holder.binding) {
            tvAulaNombre.text = aula.nombre

            val cuantos = if (aula.cantidad == 1) {
                "1 simposio"
            } else {
                "${aula.cantidad} simposios"
            }

            tvAulaDetalle.text = listOf(aula.ubicacion, cuantos)
                .filter { it.isNotBlank() }
                .joinToString(" · ")
        }
    }

    private fun pintarSimposio(
        holder: SimposioViewHolder,
        simposio: SimposioFirebase
    ) {
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

            /* El aula ya la dice el encabezado del grupo: repetirla en cada
             * tarjeta solo agrega ruido. */
            tvDuracionAdmin.text = fechas

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

    private class Diff : DiffUtil.ItemCallback<Fila>() {

        override fun areItemsTheSame(oldItem: Fila, newItem: Fila): Boolean =
            oldItem.clave == newItem.clave

        override fun areContentsTheSame(oldItem: Fila, newItem: Fila): Boolean =
            when {
                oldItem is Fila.Aula && newItem is Fila.Aula ->
                    oldItem == newItem

                oldItem is Fila.Simposio && newItem is Fila.Simposio ->
                    mismoContenido(oldItem.simposio, newItem.simposio)

                else -> false
            }

        private fun mismoContenido(
            viejo: SimposioFirebase,
            nuevo: SimposioFirebase
        ): Boolean =
            viejo.id == nuevo.id &&
                    viejo.organizadorUid == nuevo.organizadorUid &&
                    viejo.aulaId == nuevo.aulaId &&
                    viejo.aulaNombre == nuevo.aulaNombre &&
                    viejo.aulaEdificio == nuevo.aulaEdificio &&
                    viejo.aulaPiso == nuevo.aulaPiso &&
                    viejo.titulo == nuevo.titulo &&
                    viejo.descripcion == nuevo.descripcion &&
                    viejo.temaCentral == nuevo.temaCentral &&
                    viejo.fechaInicio == nuevo.fechaInicio &&
                    viejo.fechaFin == nuevo.fechaFin
    }

    companion object {
        private const val TIPO_AULA = 0
        private const val TIPO_SIMPOSIO = 1
    }
}
