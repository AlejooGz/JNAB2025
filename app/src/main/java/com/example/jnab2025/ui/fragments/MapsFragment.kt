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

    /**
     * Lugar al que hay que llevar la camara, cuando se llega desde una
     * notificacion LUGAR_AGREGADO. Queda en null una vez enfocado.
     */
    private var lugarAEnfocar: String? = null

    override fun onCreate(savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // se lee del bundle de argumentos y se borra de ahi al enfocar, asi al
        // rotar o volver atras no se repite el zoom
        lugarAEnfocar = arguments?.getString(ARG_LUGAR_ID)
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
            intentarEnfocar()
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
                                intentarEnfocar()
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

    /**
     * Lleva la camara al lugar de la notificacion y abre su detalle, como
     * cuando se busca una direccion en Google Maps. Hace falta el mapa listo y
     * que el lugar ya haya llegado de Firestore, por eso se reintenta desde los
     * dos lados (onMapReady y cada snapshot de lugares).
     */
    private fun intentarEnfocar() {
        val id = lugarAEnfocar ?: return
        if (!mapaListo) {
            return
        }
        // se busca tambien entre los inactivos para poder avisar que ya no esta
        val lugar =
            lugaresViewModel
                .todosLugares
                .value
                .firstOrNull { it.id == id }
                ?: return // todavia no llego el snapshot con ese lugar

        lugarAEnfocar = null
        arguments?.remove(ARG_LUGAR_ID)

        if (!lugar.activo) {
            Toast.makeText(
                requireContext(),
                "Ese lugar ya no está disponible en el mapa",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // con un filtro activo el marcador podria quedar oculto: se limpian y
        // el observer de filtros vuelve a dibujar todos los marcadores
        filtroViewModel.limpiarFiltros()

        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    lugar.latitud,
                    lugar.longitud
                ),
                ZOOM_LUGAR
            ),
            DURACION_ZOOM_MS,
            object : GoogleMap.CancelableCallback {
                override fun onFinish() {
                    mostrarDetalle(lugar)
                }

                // si el usuario toca el mapa a mitad de la animacion
                override fun onCancel() {
                    mostrarDetalle(lugar)
                }
            }
        )
    }

    private fun mostrarDetalle(lugar: LugarFirebase) {
        // la animacion pudo terminar con el fragment ya fuera de pantalla
        if (!isAdded || parentFragmentManager.isStateSaved) {
            return
        }
        DetalleLugarBottomSheetFragment
            .newInstance(lugar)
            .show(
                parentFragmentManager,
                "DetalleLugar"
            )
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

    companion object {
        /** Mismo nombre que el argumento de mapsFragment en nav_graph.xml. */
        const val ARG_LUGAR_ID = "lugarId"

        /** Nivel de calle, suficiente para distinguir el local. */
        private const val ZOOM_LUGAR = 17f
        private const val DURACION_ZOOM_MS = 1_500
    }
}