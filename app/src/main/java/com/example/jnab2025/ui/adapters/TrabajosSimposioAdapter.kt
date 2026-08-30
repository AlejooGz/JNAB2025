package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.data.model.EstadoTrabajo
import com.example.jnab2025.data.model.TrabajoSimposioUi
import com.example.jnab2025.databinding.ItemTrabajoSimposioBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TrabajosSimposioAdapter(
    private val onProgramarClick:
        (TrabajoSimposioUi) -> Unit
) : ListAdapter<
        TrabajoSimposioUi,
        TrabajosSimposioAdapter.ViewHolder
        >(Diff()) {

    private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    class ViewHolder(
        val binding: ItemTrabajoSimposioBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    )

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding =
            ItemTrabajoSimposioBinding.inflate(
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
        val item = getItem(position)
        val trabajo = item.trabajo
        val charla = item.charla
        with(holder.binding) {
            tvTitulo.text = trabajo.titulo
            tvAutor.text = "Expositor: ${trabajo.autorNombre}"
            when {

                trabajo.estado ==
                        EstadoTrabajo.ACEPTADO_PENDIENTE_PAGO.name -> {
                    tvEstado.text = "Aceptado · inscripción pendiente"
                    tvProgramacion.visibility = View.GONE
                    btnProgramar.visibility = View.GONE
                }
                trabajo.estado ==
                        EstadoTrabajo.APROBADO.name &&
                        charla == null -> {
                    tvEstado.text = "Aprobado · listo para programar"
                    tvProgramacion.visibility = View.GONE
                    btnProgramar.visibility = View.VISIBLE
                }

                trabajo.estado ==
                        EstadoTrabajo.APROBADO.name &&
                        charla != null -> {
                    tvEstado.text = "Programado"
                    val fecha =
                        charla.fecha
                            ?.toDate()
                            ?.toInstant()
                            ?.atZone(
                                ZoneId.systemDefault()
                            )
                            ?.toLocalDate()

                    tvProgramacion.visibility = View.VISIBLE
                    tvProgramacion.text =
                        buildString {
                            if (fecha != null) {
                                append(
                                    fecha.format(
                                        formatoFecha
                                    )
                                )
                            }

                            if (
                                charla.horaInicio.isNotBlank()
                            ) {

                                append(
                                    " · ${charla.horaInicio}"
                                )

                                if (
                                    charla.horaFin.isNotBlank()
                                ) {

                                    append(
                                        " a ${charla.horaFin}"
                                    )
                                }
                            }
                        }

                    btnProgramar.visibility = View.GONE
                }
                trabajo.estado ==
                        EstadoTrabajo.RECHAZADO.name -> {
                    tvEstado.text = "Rechazado"
                    tvProgramacion.visibility = View.GONE
                    btnProgramar.visibility = View.GONE
                }
                else -> {
                    tvEstado.text = trabajo.estado
                    tvProgramacion.visibility = View.GONE
                    btnProgramar.visibility = View.GONE
                }
            }
            btnProgramar.setOnClickListener {
                onProgramarClick(item)
            }
        }
    }
    private class Diff :
        DiffUtil.ItemCallback<
                TrabajoSimposioUi
                >() {

        override fun areItemsTheSame(
            oldItem: TrabajoSimposioUi,
            newItem: TrabajoSimposioUi
        ): Boolean {
            return oldItem.trabajo.id ==
                    newItem.trabajo.id
        }

        override fun areContentsTheSame(
            oldItem: TrabajoSimposioUi,
            newItem: TrabajoSimposioUi
        ): Boolean {
            return oldItem == newItem
        }
    }
}