package com.example.jnab2025.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jnab2025.databinding.FragmentVerInscriptosBinding
import com.example.jnab2025.models.User
import com.example.jnab2025.ui.adapters.InscriptosAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class VerInscriptosFragment : Fragment() {

    private var _binding: FragmentVerInscriptosBinding? = null
    private val binding get() = _binding!!

    private val gson = Gson()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVerInscriptosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val prefs = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val json = prefs.getString("inscriptos", "[]")
        val listType = object : TypeToken<List<User>>() {}.type
        val usuarios = gson.fromJson<List<User>>(json, listType)

        val adapter = InscriptosAdapter(usuarios)
        binding.recyclerInscriptos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerInscriptos.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
