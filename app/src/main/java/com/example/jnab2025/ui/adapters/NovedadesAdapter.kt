package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jnab2025.R
import com.example.jnab2025.data.model.NovedadFirebase
import java.text.SimpleDateFormat
import java.util.Locale

class NovedadesAdapter :
    ListAdapter<
            NovedadFirebase,
            NovedadesAdapter.NovedadViewHolder
            >(Diff()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NovedadViewHolder {

        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_novedad,
                parent,
                false
            )
        return NovedadViewHolder(view)
    }
    override fun onBindViewHolder(
        holder: NovedadViewHolder,
        position: Int
    ) {
        holder.bind(
            getItem(position)
        )
    }
    class NovedadViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {
        private val ivImagenNovedad: ImageView = itemView.findViewById(
                R.id.ivImagenNovedad
        )
        private val tvTitulo: TextView = itemView.findViewById(
                R.id.tvTitulo
        )
        private val tvDescripcion: TextView = itemView.findViewById(
                R.id.tvDescripcion
        )
        private val tvFecha: TextView = itemView.findViewById(
                R.id.tvFecha
        )
        fun bind(novedad: NovedadFirebase
        ) {
            tvTitulo.text = novedad.titulo
            tvDescripcion.text = novedad.descripcion
            tvFecha.text = formatearFecha(novedad)
            mostrarImagen(novedad.imagenUrl)
        }
        private fun mostrarImagen(imagenUrl: String?) {
            if (imagenUrl.isNullOrBlank()) {
                ivImagenNovedad.visibility = View.GONE
                Glide.with(itemView).clear(ivImagenNovedad)
                return
            }
            ivImagenNovedad.visibility = View.VISIBLE
            Glide
                .with(itemView)
                .load(imagenUrl)
                .centerCrop()
                .into(ivImagenNovedad)
        }
        private fun formatearFecha(novedad: NovedadFirebase
        ): String {
            val fecha = novedad.fechaPublicacion
                    ?.toDate()
                    ?: return ""
            val formato = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                )
            return formato.format(fecha)
        }
    }
    private class Diff :
        DiffUtil.ItemCallback<NovedadFirebase>() {
        override fun areItemsTheSame(
            oldItem: NovedadFirebase,
            newItem: NovedadFirebase
        ): Boolean {
            return oldItem.id ==
                    newItem.id
        }
        override fun areContentsTheSame(
            oldItem: NovedadFirebase,
            newItem: NovedadFirebase
        ): Boolean {
            return oldItem ==
                    newItem
        }
    }
}