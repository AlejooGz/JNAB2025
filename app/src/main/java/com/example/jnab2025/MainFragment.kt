package com.example.jnab2025

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jnab2025.databinding.FragmentMainBinding
import com.example.jnab2025.utils.SesionUsuario

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val rol = SesionUsuario.obtenerRol(requireContext())
        configurarVistaComun()

        when {
            SesionUsuario.esExpositor(requireContext()) -> configurarVistaExpositor()
            SesionUsuario.esOrganizador(requireContext()) -> configurarVistaOrganizador()
            SesionUsuario.esAsistente(requireContext()) -> configurarVistaAsistente()
        }
    }

    private fun configurarVistaComun() {
        // Ocultar todos los botones
        // binding.btnTramite.visibility = View.GONE
        // binding.btnMisTramites.visibility = View.GONE
        // binding.btnMisSimposios.visibility = View.GONE
        // binding.btnVerInscriptos.visibility = View.GONE
        // binding.btnInscribirse.visibility = View.GONE
        // binding.btnListSimposios.visibility = View.VISIBLE
    }

    private fun configurarVistaExpositor() {
        Toast.makeText(requireContext(), "Bienvenido Expositor", Toast.LENGTH_SHORT).show()

        // binding.btnTramite.visibility = View.VISIBLE
        // binding.btnMisTramites.visibility = View.VISIBLE

        // binding.btnTramite.setOnClickListener {
        //     findNavController().navigate(R.id.tramiteExpositorFragment)
        // }

        // binding.btnMisTramites.setOnClickListener {
        //     findNavController().navigate(R.id.seguimientoTramiteFragment)
        // }
    }

    private fun configurarVistaOrganizador() {
        Toast.makeText(requireContext(), "Bienvenido Organizador", Toast.LENGTH_SHORT).show()

        // binding.btnMisSimposios.visibility = View.VISIBLE
        // binding.btnVerInscriptos.visibility = View.VISIBLE

        // binding.btnMisSimposios.setOnClickListener {
        //     findNavController().navigate(R.id.misSimposiosFragment)
        // }

        // binding.btnVerInscriptos.setOnClickListener {
        //     findNavController().navigate(R.id.verInscriptosFragment)
        // }
    }

    private fun configurarVistaAsistente() {
        Toast.makeText(requireContext(), "Bienvenido Asistente", Toast.LENGTH_SHORT).show()

        // binding.btnInscribirse.visibility = View.VISIBLE

        // binding.btnInscribirse.setOnClickListener {
        //     findNavController().navigate(R.id.inscripcionFragment)
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}