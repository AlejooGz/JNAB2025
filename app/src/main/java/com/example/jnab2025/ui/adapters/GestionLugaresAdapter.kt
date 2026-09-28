package com.example.jnab2025.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.jnab2025.data.model.CategoriaLugar
import com.example.jnab2025.data.model.LugarFirebase
import com.example.jnab2025.databinding.ItemLugarGestionBinding

class GestionLugaresAdapter(
    private val onEditar: (LugarFirebase) -> Unit,
    private val onCambiarEstado: (LugarFirebase) -> Unit
) : RecyclerView.Adapter<
        GestionLugaresAdapter.ViewHolder>() {
    private val lugares = mutableListOf<LugarFirebase>()

    fun actualizar(
        nuevaLista:
        List<LugarFirebase>
    ) {
        lugares.clear()
        lugares.addAll(
            nuevaLista
        )
        notifyDataSetChanged()
    }
    inner class ViewHolder(
        private val binding:
        ItemLugarGestionBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(
            lugar: LugarFirebase
        ) {
            binding.tvNombre.text = lugar.nombre
            binding.tvCategoria.text = when (
                    lugar.categoriaEnum()
                ) {
                    CategoriaLugar.HOSPEDAJE -> "Hospedaje"
                    CategoriaLugar.RESTAURANTE -> "Restaurante"
                    CategoriaLugar.AGENCIA -> "Agencia"
                }
            binding.tvDireccion.text = lugar.direccion
            binding.tvEstado.text =
                if (lugar.activo) {
                    "Activo"
                } else {
                    "Inactivo"
                }
            binding.btnEstado.text =
                if (lugar.activo) {
                    "Desactivar"
                } else {
                    "Activar"
                }
            binding.btnEditar.setOnClickListener {
                    onEditar(
                        lugar
                    )
                }
            binding.btnEstado.setOnClickListener {
                    onCambiarEstado(
                        lugar
                    )
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemLugarGestionBinding
                .inflate(
                    LayoutInflater
                        .from(
                            parent.context
                        ),
                    parent,
                    false
                )
        return ViewHolder(
            binding
        )
    }
    override fun onBindViewHolder(holder: ViewHolder,
        position: Int
    ) {
        holder.bind(lugares[position]
        )
    }
    override fun getItemCount():
            Int = lugares.size
}