package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.example.jnab2025.R
import com.example.jnab2025.models.Categoria
import com.example.jnab2025.ui.viewmodels.FiltroViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import androidx.lifecycle.ViewModelProvider

class FiltroBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var filtroViewModel: FiltroViewModel
    private lateinit var btnHospedaje: MaterialButton
    private lateinit var btnRestaurante: MaterialButton
    private lateinit var btnAgencia: MaterialButton
    private lateinit var btnCerrar: ImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_filtro_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filtroViewModel = ViewModelProvider(requireActivity())[FiltroViewModel::class.java]

        btnHospedaje = view.findViewById(R.id.btnHospedaje)
        btnRestaurante = view.findViewById(R.id.btnRestaurante)
        btnAgencia = view.findViewById(R.id.btnAgencia)
        btnCerrar = view.findViewById(R.id.btnCerrar)

        btnHospedaje.setOnClickListener {
            filtroViewModel.aplicarFiltro(Categoria.HOSPEDAJE)
            dismiss()
        }

        btnRestaurante.setOnClickListener {
            filtroViewModel.aplicarFiltro(Categoria.RESTAURANTE)
            dismiss()
        }

        btnAgencia.setOnClickListener {
            filtroViewModel.aplicarFiltro(Categoria.AGENCIA)
            dismiss()
        }

        btnCerrar.setOnClickListener {
            filtroViewModel.limpiarFiltro()
            dismiss()
        }
    }
}
