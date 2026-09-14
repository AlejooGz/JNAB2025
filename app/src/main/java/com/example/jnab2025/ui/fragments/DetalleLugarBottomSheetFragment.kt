package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.jnab2025.data.model.CategoriaLugar
import com.example.jnab2025.data.model.LugarFirebase
import com.example.jnab2025.databinding.FragmentDetalleLugarBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DetalleLugarBottomSheetFragment :
    BottomSheetDialogFragment() {
    private var _binding: FragmentDetalleLugarBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentDetalleLugarBottomSheetBinding.inflate(
                inflater,
                container,
                false
            )
        return binding.root
    }
    override fun onViewCreated(view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val nombre = requireArguments().getString(ARG_NOMBRE).orEmpty()
        val categoria = requireArguments().getString(ARG_CATEGORIA).orEmpty()
        val descuento = requireArguments().getString(ARG_DESCUENTO).orEmpty()
        val direccion = requireArguments().getString(ARG_DIRECCION).orEmpty()
        val telefono = requireArguments().getString(ARG_TELEFONO).orEmpty()
        val email = requireArguments().getString(ARG_EMAIL).orEmpty()
        val web = requireArguments().getString(ARG_WEB).orEmpty()

        binding.tvNombreLugar.text = nombre
        binding.tvCategoriaLugar.text =
            when (categoria) {
                CategoriaLugar.HOSPEDAJE.name -> "Hospedaje"
                CategoriaLugar.RESTAURANTE.name -> "Restaurante"
                CategoriaLugar.AGENCIA.name -> "Agencia"
                else ->
                    categoria
            }
        binding.tvDescuentoLugar.text =
            if (descuento.isBlank()) {
                "Consultar beneficio"
            } else {
                descuento
            }
        configurarDato(
            binding.contenedorDireccion,
            binding.tvDireccionLugar,
            direccion
        )
        configurarDato(
            binding.contenedorTelefono,
            binding.tvTelefonoLugar,
            telefono
        )
        configurarDato(
            binding.contenedorEmail,
            binding.tvEmailLugar,
            email
        )
        configurarDato(
            binding.contenedorWeb,
            binding.tvWebLugar,
            web
        )
        binding.btnCerrar.setOnClickListener { dismiss()
        }
    }
    private fun configurarDato(
        contenedor: View,
        textView: android.widget.TextView,
        valor: String
    ) {
        if (valor.isBlank()) {
            contenedor.visibility = View.GONE
        } else {
            contenedor.visibility = View.VISIBLE
            textView.text = valor
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    companion object {
        private const val ARG_NOMBRE = "nombre"
        private const val ARG_CATEGORIA = "categoria"
        private const val ARG_DESCUENTO = "descuento"
        private const val ARG_DIRECCION = "direccion"
        private const val ARG_TELEFONO = "telefono"
        private const val ARG_EMAIL = "email"
        private const val ARG_WEB = "web"

        fun newInstance(
            lugar: LugarFirebase
        ): DetalleLugarBottomSheetFragment {
            return DetalleLugarBottomSheetFragment().apply {
                arguments =
                    Bundle().apply { putString(ARG_NOMBRE, lugar.nombre)
                        putString(ARG_CATEGORIA, lugar.categoria)
                        putString(ARG_DESCUENTO, lugar.descuento)
                        putString(ARG_DIRECCION, lugar.direccion)
                        putString(ARG_TELEFONO, lugar.telefono)
                        putString(ARG_EMAIL, lugar.email)
                        putString(ARG_WEB, lugar.web)
                    }
            }
        }
    }
}