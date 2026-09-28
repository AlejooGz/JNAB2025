package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.R
import com.example.jnab2025.data.model.EstadoComprobante
import com.example.jnab2025.data.model.EstadoInscripcion
import com.example.jnab2025.data.model.InscriptoSeguimientoFirebase

class InscriptosAdapter(
    private val onVerComprobante: (InscriptoSeguimientoFirebase) -> Unit,
    private val onVerificar: (InscriptoSeguimientoFirebase) -> Unit,
    private val onRechazar: (InscriptoSeguimientoFirebase) -> Unit

) : ListAdapter<
        InscriptoSeguimientoFirebase,
        InscriptosAdapter.ViewHolder
        >(Diff()) {

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val nombre: TextView = view.findViewById(R.id.tvNombre)
        val email: TextView = view.findViewById(R.id.tvEmail)
        val rol: TextView = view.findViewById(R.id.tvRol)
        val estadoPago: TextView = view.findViewById(R.id.tvEstadoPago)
        val comprobante: TextView = view.findViewById(R.id.tvComprobante)
        val verComprobante: Button = view.findViewById(R.id.btnVerComprobante)
        val verificar: Button = view.findViewById(R.id.btnVerificar)
        val rechazar: Button = view.findViewById(R.id.btnRechazar)
        val acciones: LinearLayout = view.findViewById(R.id.contenedorAcciones)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_inscripto,
                    parent,
                    false
                )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val seguimiento = getItem(position)
        val inscripcion = seguimiento.inscripcion
        val comprobante = seguimiento.comprobante

        holder.nombre.text =
            inscripcion.usuarioNombre

        holder.email.text =
            inscripcion.usuarioEmail

        val tipo =
            inscripcion.tipo
                .lowercase()
                .replaceFirstChar {
                    it.uppercase()
                }

        val categoria =
            inscripcion.categoria
                .lowercase()
                .replaceFirstChar {
                    it.uppercase()
                }
        holder.rol.text =
            "$tipo · $categoria"

        holder.estadoPago.text =
            when (inscripcion.estado) {
                EstadoInscripcion.PAGADA.name -> "Inscripción acreditada"
                EstadoInscripcion.PENDIENTE_PAGO.name -> "Inscripción pendiente de pago"
                EstadoInscripcion.ANULADA.name -> "Inscripción anulada"

                else -> inscripcion.estado
            }

        if (comprobante == null) {
            holder.comprobante.text = "Todavía no cargó comprobante"
            holder.verComprobante.visibility = View.GONE
            holder.acciones.visibility = View.GONE
        } else {
            holder.verComprobante.visibility =
                View.VISIBLE
            holder.comprobante.text =
                when (comprobante.estado) {
                    EstadoComprobante.PENDIENTE.name ->
                        "Comprobante pendiente de verificación"
                    EstadoComprobante.VERIFICADO.name ->
                        "Comprobante verificado"
                    EstadoComprobante.RECHAZADO.name ->
                        "Comprobante rechazado"
                    else -> comprobante.estado
                }

            // Verificar/rechazar solo tiene sentido mientras el comprobante esté pendiente.
            holder.acciones.visibility =
                if (
                    comprobante.estado ==
                    EstadoComprobante.PENDIENTE.name
                ) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
        }

        holder.verComprobante.setOnClickListener {
            onVerComprobante(seguimiento)
        }

        holder.verificar.setOnClickListener {
            onVerificar(seguimiento)
        }

        holder.rechazar.setOnClickListener {
            onRechazar(seguimiento)
        }
    }
    private class Diff :
        DiffUtil.ItemCallback<
                InscriptoSeguimientoFirebase
                >() {

        override fun areItemsTheSame(
            oldItem: InscriptoSeguimientoFirebase,
            newItem: InscriptoSeguimientoFirebase
        ): Boolean {

            return oldItem.inscripcion.id ==
                    newItem.inscripcion.id
        }

        override fun areContentsTheSame(
            oldItem: InscriptoSeguimientoFirebase,
            newItem: InscriptoSeguimientoFirebase
        ): Boolean {

            return oldItem == newItem
        }
    }
}