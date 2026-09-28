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
import com.example.jnab2025.databinding.ItemNovedadBinding
class NovedadesAdapter(
    private val esOrganizador: Boolean,
    private val onEditar: (NovedadFirebase) -> Unit,
    private val onEliminar: (NovedadFirebase) -> Unit
) : ListAdapter<NovedadFirebase, NovedadesAdapter.NovedadViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NovedadViewHolder {

        val binding = ItemNovedadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return NovedadViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NovedadViewHolder,
        position: Int
    ) {
        holder.bind(getItem(position))
    }

    inner class NovedadViewHolder(
        private val binding: ItemNovedadBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(novedad: NovedadFirebase) {

            binding.tvTitulo.text = novedad.titulo
            binding.tvDescripcion.text = novedad.descripcion

            novedad.fechaPublicacion?.let { timestamp ->
                val formato = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm",
                    Locale.getDefault()
                )

                binding.tvFecha.text =
                    "Publicado el ${formato.format(timestamp.toDate())}"
            } ?: run {
                binding.tvFecha.text = ""
            }

            if (!novedad.imagenUrl.isNullOrBlank()) {

                binding.ivImagenNovedad.visibility = View.VISIBLE

                Glide.with(binding.root.context)
                    .load(novedad.imagenUrl)
                    .centerCrop()
                    .into(binding.ivImagenNovedad)

            } else {

                binding.ivImagenNovedad.visibility = View.GONE
            }

            if (esOrganizador) {

                binding.layoutAccionesNovedad.visibility = View.VISIBLE

                binding.btnEditarNovedad.setOnClickListener {
                    onEditar(novedad)
                }

                binding.btnEliminarNovedad.setOnClickListener {
                    onEliminar(novedad)
                }

            } else {

                binding.layoutAccionesNovedad.visibility = View.GONE
                // evitamos que quede un listener de un elemento reciclado
                binding.btnEditarNovedad.setOnClickListener(null)
                binding.btnEliminarNovedad.setOnClickListener(null)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<NovedadFirebase>() {

        override fun areItemsTheSame(
            oldItem: NovedadFirebase,
            newItem: NovedadFirebase
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: NovedadFirebase,
            newItem: NovedadFirebase
        ): Boolean {
            return oldItem == newItem
        }
    }
}