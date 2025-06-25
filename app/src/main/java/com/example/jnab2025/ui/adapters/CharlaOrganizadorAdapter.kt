package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.databinding.ItemPropuestaBinding
import com.example.jnab2025.models.Charla
import com.example.jnab2025.models.User

class CharlaOrganizadorAdapter(
    private val charlasPendientes: List<Charla>,
    private val usuarios: List<User>,
    private val onAceptarClick: (Charla) -> Unit,
    private val onRechazarClick: (Charla) -> Unit
) : RecyclerView.Adapter<CharlaOrganizadorAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemPropuestaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPropuestaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val charla = charlasPendientes[position]
        val expositorNombre = usuarios.find { it.id == charla.expositorId }?.username ?: "Desconocido"

        with(holder.binding) {
            tvTituloPropuesta.text = charla.titulo
            tvExpositor.text = "Expositor: $expositorNombre"
            tvDescripcionPropuesta.text = charla.descripcion

            btnAceptarPropuesta.setOnClickListener {
                onAceptarClick(charla)
            }

            btnRechazarPropuesta.setOnClickListener {
                onRechazarClick(charla)
            }
        }
    }

    override fun getItemCount(): Int = charlasPendientes.size
}
