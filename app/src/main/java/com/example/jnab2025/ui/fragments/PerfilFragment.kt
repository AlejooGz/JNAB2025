package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.jnab2025.databinding.FragmentPerfilBinding
import com.example.jnab2025.utils.Sesion

class PerfilFragment : Fragment() {
    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!Sesion.haySesion(requireContext())) {
            Toast.makeText(requireContext(), "Debes iniciar sesion", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressedDispatcher.onBackPressed()
            return
        }

        binding.etUsername.setText(Sesion.nombre(requireContext()))
        binding.etRol.setText(
            Sesion.roles(requireContext()).joinToString(" / ") { rol ->
                rol.name.lowercase().replaceFirstChar { it.uppercase() }
            }
        )

        // El rol se deriva de la tabla usuario_rol, no es algo que el usuario
        // pueda editarse a si mismo como pasaba antes.
        binding.etUsername.isEnabled = false
        binding.etRol.isEnabled = false
        binding.btnGuardar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
