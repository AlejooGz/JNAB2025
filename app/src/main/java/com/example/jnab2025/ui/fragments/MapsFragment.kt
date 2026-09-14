package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.jnab2025.R
import com.example.jnab2025.data.model.CategoriaLugar
import com.example.jnab2025.data.model.LugarFirebase
import com.example.jnab2025.ui.viewmodels.FiltroViewModel
import com.example.jnab2025.ui.viewmodels.LugaresViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapsFragment : Fragment() {
    private lateinit var googleMap: GoogleMap
    private lateinit var filtroViewModel: FiltroViewModel
    private val lugaresViewModel: LugaresViewModel by viewModels()
    private var todosLosLugares: List<LugarFirebase> = emptyList()
    private val marcadoresVisibles = mutableListOf<Marker>()
    private var mapaListo = false

    override fun onCreate(savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu,
        inflater: MenuInflater
    ) {
        inflater.inflate(
            R.menu.maps_menu,
            menu
        )
    }

    override fun onOptionsItemSelected(item: MenuItem
    ): Boolean {
        return when (
            item.itemId
        ) {
            R.id.action_filtrar -> { FiltroBottomSheetFragment()
                    .show(
                        parentFragmentManager,
                        "FiltroBottomSheet"
                    )
                true
            }
            else ->
                super.onOptionsItemSelected(item)
        }
    }
    private val callback = OnMapReadyCallback { map ->
            googleMap = map
            mapaListo = true
            val madryn = LatLng(
                    -42.7692,
                    -65.0385
                )
            googleMap.moveCamera(CameraUpdateFactory
                    .newLatLngZoom(
                        madryn,
                        14f
                    )
            )
            googleMap.setOnMarkerClickListener { marker ->
                val lugar = marker.tag
                            as? LugarFirebase
                if (lugar != null) {
                    DetalleLugarBottomSheetFragment
                        .newInstance(lugar)
                        .show(
                            parentFragmentManager,
                            "DetalleLugar"
                        )
                }
                true
            }
            aplicarFiltros()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.fragment_maps,
            container,
            false
        )
    }

    override fun onViewCreated(view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view,
            savedInstanceState
        )
        filtroViewModel = ViewModelProvider(
                requireActivity()
            )[FiltroViewModel::class.java]
        filtroViewModel
            .filtrosSeleccionados
            .observe(
                viewLifecycleOwner
            ) {
                aplicarFiltros()
            }
        observarLugares()
        observarAvisos()

        val mapFragment = childFragmentManager
                .findFragmentById(
                    R.id.map
                ) as?
                    SupportMapFragment
        mapFragment?.getMapAsync(
            callback
        )
    }
    private fun observarLugares() {
        viewLifecycleOwner
            .lifecycleScope
            .launch {
                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {
                        lugaresViewModel
                            .lugares
                            .collect { lugares ->
                                todosLosLugares =
                                    lugares
                                aplicarFiltros()
                            }
                    }
            }
    }

    private fun observarAvisos() {
        viewLifecycleOwner
            .lifecycleScope
            .launch {
                viewLifecycleOwner
                    .repeatOnLifecycle(
                        Lifecycle.State.STARTED
                    ) {
                        lugaresViewModel
                            .avisos
                            .collect { mensaje ->
                                Toast.makeText(
                                    requireContext(),
                                    mensaje,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    }
            }
    }

    private fun aplicarFiltros() {
        if (!mapaListo) {
            return
        }
        val categorias = filtroViewModel
                .filtrosSeleccionados
                .value
                ?: emptySet()
        val lugaresMostrar =
            if (
                categorias.isEmpty()
            ) {
                todosLosLugares
            } else {
                todosLosLugares.filter {
                        lugar ->
                    lugar
                        .categoriaEnum() in
                            categorias
                }
            }
        mostrarLugares(
            lugaresMostrar
        )
    }

    private fun mostrarLugares(lugares: List<LugarFirebase>
    ) {
        marcadoresVisibles
            .forEach {
                it.remove()
            }
        marcadoresVisibles
            .clear()
        lugares.forEach { lugar ->
            val color =
                when (
                    lugar.categoriaEnum()
                ) {
                    CategoriaLugar.HOSPEDAJE -> BitmapDescriptorFactory
                            .HUE_MAGENTA
                    CategoriaLugar.RESTAURANTE -> BitmapDescriptorFactory
                            .HUE_ORANGE
                    CategoriaLugar.AGENCIA -> BitmapDescriptorFactory
                            .HUE_CYAN
                }
            val marker = googleMap.addMarker(
                    MarkerOptions()
                        .position(
                            LatLng(
                                lugar.latitud,
                                lugar.longitud
                            )
                        )
                        .title(
                            lugar.nombre
                        )
                        .icon(
                            BitmapDescriptorFactory
                                .defaultMarker(
                                    color
                                )
                        )
                )
            marker?.let {
                it.tag =
                    lugar
                marcadoresVisibles
                    .add(it)
            }
        }
    }
}