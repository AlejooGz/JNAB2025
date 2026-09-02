package com.example.jnab2025

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.jnab2025.databinding.FragmentMainBinding
import com.example.jnab2025.ui.fragments.HomeOrganizadorFragment
import com.example.jnab2025.utils.Sesion

class MainFragment : Fragment() {

    private var _binding:
            FragmentMainBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMainBinding.inflate(
            inflater,
            container,
            false
        )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        mostrarHomeSegunRol()
    }

    private fun mostrarHomeSegunRol() {
        when {
            Sesion.esOrganizador(requireContext()) -> {
                binding.tvHomePendiente.visibility =
                    View.GONE

                childFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.homeContainer,
                        HomeOrganizadorFragment()
                    )
                    .commit()
            }

            Sesion.esExpositor(requireContext()) -> {
                mostrarHomePendiente(
                    "Home del expositor"
                )
            }

            Sesion.esAsistente(requireContext()) -> {
                mostrarHomePendiente(
                    "Home del asistente"
                )
            }

            else -> {
                mostrarHomePendiente(
                    "No se encontró un rol para este usuario."
                )
            }
        }
    }

    private fun mostrarHomePendiente(
        titulo: String
    ) {
        /*
         * Remueve un home anterior si el usuario cambió
         * de sesión o rol.
         */
        childFragmentManager
            .findFragmentById(
                R.id.homeContainer
            )
            ?.let { fragmento ->

                childFragmentManager
                    .beginTransaction()
                    .remove(fragmento)
                    .commit()
            }

        binding.tvHomePendiente.text =
            "$titulo\n\nPróximamente construiremos esta pantalla."

        binding.tvHomePendiente.visibility =
            View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}