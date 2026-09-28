package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.data.model.FaqFirebase
import com.example.jnab2025.databinding.ItemFaqGestionBinding
class GestionFaqAdapter(
    private val onEditarClick: (FaqFirebase) -> Unit,
    private val onEliminarClick: (FaqFirebase) -> Unit
) : RecyclerView.Adapter<GestionFaqAdapter.FaqViewHolder>() {
    private val items = mutableListOf<FaqFirebase>()

    fun actualizar(nuevaLista: List<FaqFirebase>) {
        items.clear()
        items.addAll(nuevaLista)
        notifyDataSetChanged()
    }
    inner class FaqViewHolder(private val binding: ItemFaqGestionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(faq: FaqFirebase) {
            binding.tvPregunta.text = faq.pregunta
            binding.tvRespuesta.text = faq.respuesta
            binding.tvPublico.text = when (faq.publico) {
                "ASISTENTE" -> "Asistentes"
                "EXPOSITOR" -> "Expositores"
                else -> faq.publico
            }
            binding.tvOrden.text = "Orden: ${faq.orden}"
            binding.btnEditar.setOnClickListener { onEditarClick(faq)
            }
            binding.btnEliminar.setOnClickListener { onEliminarClick(faq)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FaqViewHolder {
        val binding = ItemFaqGestionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: FaqViewHolder,
        position: Int
    ) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}