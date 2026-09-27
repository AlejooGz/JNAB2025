package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.data.model.ActividadFirebase
import com.example.jnab2025.data.model.TipoActividad

class ActividadAdapter(
    private val onEditarClick: (ActividadFirebase) -> Unit,
    private val onEliminarClick: (ActividadFirebase) -> Unit
) : ListAdapter<
        ActividadFirebase,
        ActividadAdapter.ViewHolder
        >(Diff()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.item_actividad_organizador,
                    parent,
                    false
                )
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder,
        position: Int
    ) {
        holder.bind(
            getItem(position)
        )
    }
    inner class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val tvTitulo: TextView = itemView.findViewById(
                R.id.tvTituloActividad
        )
        private val tvTipo: TextView = itemView.findViewById(
                R.id.tvTipoActividad
        )
        private val tvFecha: TextView = itemView.findViewById(
                R.id.tvFechaActividad
        )
        private val tvHorario: TextView = itemView.findViewById(
                R.id.tvHorarioActividad
        )
        private val tvAula: TextView = itemView.findViewById(
                R.id.tvAulaActividad
        )
        private val btnEditar: TextView = itemView.findViewById(
                R.id.btnEditarActividad
        )
        private val btnEliminar: TextView = itemView.findViewById(
                R.id.btnEliminarActividad
        )

        fun bind(
            actividad: ActividadFirebase
        ) {
            tvTitulo.text = actividad.titulo
            tvTipo.text =
                when (actividad.tipo) {
                    TipoActividad.CONFERENCIA.name -> "Conferencia"
                    TipoActividad.COFFEE_BREAK.name -> "Coffee break"
                    TipoActividad.ACREDITACION.name -> "Acreditación"
                    TipoActividad.PRESENTACION.name -> "Presentación"
                    else ->
                        "Otra actividad"
                }
            tvFecha.text = actividad.fecha
                    ?.toDate()
                    ?.let {

                        java.text.SimpleDateFormat(
                            "dd/MM/yyyy",
                            java.util.Locale("es", "AR")
                        ).format(it)
                    }
                    ?: "Fecha no disponible"
            tvHorario.text =
                if (
                    actividad.horaInicio.isNotBlank() &&
                    actividad.horaFin.isNotBlank()
                ) {
                    "${actividad.horaInicio} - " + actividad.horaFin

                } else {
                    "Horario no disponible"
                }
            tvAula.text =
                if (
                    actividad.aulaNombre.isNotBlank()
                ) {
                    buildString {
                        append(
                            actividad.aulaNombre
                        )
                        if (
                            actividad.aulaEdificio
                                .isNotBlank()
                        ) {
                            append(
                                " · ${actividad.aulaEdificio}"
                            )
                        }
                        append(
                            if (
                                actividad.aulaPiso == 0
                            ) {
                                " · planta baja"
                            } else {
                                " · piso ${actividad.aulaPiso}"
                            }
                        )
                    }
                } else {

                    "Actividad general · sin aula"
                }
            btnEditar.setOnClickListener {
                onEditarClick(actividad)
            }

            btnEliminar.setOnClickListener {
                onEliminarClick(actividad)
            }
        }
    }
    private class Diff :
        DiffUtil.ItemCallback<ActividadFirebase>() {
        override fun areItemsTheSame(
            oldItem: ActividadFirebase,
            newItem: ActividadFirebase
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ActividadFirebase,
            newItem: ActividadFirebase
        ): Boolean {
            return oldItem == newItem
        }
    }
}