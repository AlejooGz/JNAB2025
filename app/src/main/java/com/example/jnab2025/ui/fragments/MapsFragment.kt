package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.jnab2025.R
import com.example.jnab2025.models.Categoria
import com.example.jnab2025.models.Lugar
import com.example.jnab2025.ui.viewmodels.FiltroViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment() {

    private lateinit var googleMap: GoogleMap
    private lateinit var filtroViewModel: FiltroViewModel
    private lateinit var todosLosLugares: List<Lugar>
    private val marcadoresVisibles = mutableListOf<com.google.android.gms.maps.model.Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true) // Habilita el ícono de filtro en la toolbar
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.maps_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_filtrar -> {
                FiltroBottomSheetFragment().show(parentFragmentManager, "FiltroBottomSheet")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val callback = OnMapReadyCallback { map ->
        googleMap = map
        val madryn = LatLng(-42.7692, -65.0385)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madryn, 14f))

        todosLosLugares = cargarLugares()
        mostrarLugares(todosLosLugares)
    }

    private fun cargarLugares(): List<Lugar> = listOf(
        Lugar("Hotel Piren", LatLng(-42.7729, -65.0331), Categoria.HOSPEDAJE),
        Lugar("Hotel Península", LatLng(-42.7734, -65.0320), Categoria.HOSPEDAJE),
        Lugar("Hotel Yene Hue", LatLng(-42.7736, -65.0305), Categoria.HOSPEDAJE),
        Lugar("Hotel La Posada", LatLng(-42.7702, -65.0201), Categoria.HOSPEDAJE),
        Lugar("Hotel Tandil", LatLng(-42.7641, -65.0377), Categoria.HOSPEDAJE),
        Lugar("Hostel La Tosca", LatLng(-42.7712, -65.0375), Categoria.HOSPEDAJE),
        Lugar("Complejo El Puente", LatLng(-42.7718, -65.0452), Categoria.HOSPEDAJE),
        Lugar("Complejo Aires del Mar", LatLng(-42.7685, -65.0427), Categoria.HOSPEDAJE),
        Lugar("Complejo Las Tablas", LatLng(-42.7730, -65.0444), Categoria.HOSPEDAJE),
        Lugar("Complejo Manu", LatLng(-42.7731, -65.0413), Categoria.HOSPEDAJE),
        Lugar("Complejo Quilimbai", LatLng(-42.7705, -65.0289), Categoria.HOSPEDAJE),
        Lugar("Complejo Plas Hedd", LatLng(-42.7679, -65.0265), Categoria.HOSPEDAJE),
        Lugar("La Casa del Pino", LatLng(-42.7703, -65.0279), Categoria.HOSPEDAJE),
        Lugar("Abedules Madryn", LatLng(-42.7661, -65.0383), Categoria.HOSPEDAJE),
        Lugar("Las Anémonas", LatLng(-42.7697, -65.0370), Categoria.HOSPEDAJE),
        Lugar("Los Buenos Días", LatLng(-42.7680, -65.0305), Categoria.HOSPEDAJE),
        Lugar("Mar y Arena", LatLng(-42.7727, -65.0431), Categoria.HOSPEDAJE),
        Lugar("AB Alojamientos", LatLng(-42.7672, -65.0271), Categoria.HOSPEDAJE),
        Lugar("Abadía", LatLng(-42.7687, -65.0361), Categoria.HOSPEDAJE),
        Lugar("Arenas de la Patagonia", LatLng(-42.7709, -65.0287), Categoria.HOSPEDAJE),
        Lugar("Cabañas Talismán Azul", LatLng(-42.7726, -65.0372), Categoria.HOSPEDAJE),
        Lugar("Cabañas El Jacaranda", LatLng(-42.7693, -65.0262), Categoria.HOSPEDAJE),
        Lugar("Parrilla Big David", LatLng(-42.7720, -65.0405), Categoria.RESTAURANTE),
        Lugar("Bistró de Mar", LatLng(-42.7745, -65.0241), Categoria.RESTAURANTE),
        Lugar("Fervor", LatLng(-42.7684, -65.0301), Categoria.RESTAURANTE),
        Lugar("All Península", LatLng(-42.7735, -65.0340), Categoria.AGENCIA),
        Lugar("Argentina Visión", LatLng(-42.7741, -65.0327), Categoria.AGENCIA),
        Lugar("Arrieros Patagónicos", LatLng(-42.7743, -65.0325), Categoria.AGENCIA),
        Lugar("Animal Travel", LatLng(-42.7742, -65.0331), Categoria.AGENCIA),
        Lugar("Cuyun Co", LatLng(-42.7730, -65.0321), Categoria.AGENCIA),
        Lugar("Catalejo", LatLng(-42.7689, -65.0380), Categoria.AGENCIA),
        Lugar("Categoría Patagonia", LatLng(-42.7729, -65.0333), Categoria.AGENCIA),
        Lugar("Cerac Travel", LatLng(-42.7694, -65.0400), Categoria.AGENCIA),
        Lugar("Chucao", LatLng(-42.7708, -65.0361), Categoria.AGENCIA),
        Lugar("El Pedral", LatLng(-42.7734, -65.0338), Categoria.AGENCIA),
        Lugar("Explore Patagonia", LatLng(-42.7696, -65.0425), Categoria.AGENCIA),
        Lugar("El Médano", LatLng(-42.7693, -65.0376), Categoria.AGENCIA),
        Lugar("Euforia", LatLng(-42.7652, -65.0389), Categoria.AGENCIA),
        Lugar("Flamenco Tour", LatLng(-42.7693, -65.0402), Categoria.AGENCIA),
        Lugar("Flores Viajes", LatLng(-42.7687, -65.0302), Categoria.AGENCIA),
        Lugar("Forastero Tour", LatLng(-42.7689, -65.0378), Categoria.AGENCIA),
        Lugar("Fugutours", LatLng(-42.7738, -65.0318), Categoria.AGENCIA),
        Lugar("Julidays", LatLng(-42.7621, -65.0343), Categoria.AGENCIA),
        Lugar("Huinca Travel", LatLng(-42.7689, -65.0379), Categoria.AGENCIA),
        Lugar("Montaner", LatLng(-42.7654, -65.0387), Categoria.AGENCIA),
        Lugar("Odisea Patagónica", LatLng(-42.7742, -65.0330), Categoria.AGENCIA),
        Lugar("Punta Tombo Turismo", LatLng(-42.7842, -65.0203), Categoria.AGENCIA),
        Lugar("Rústica", LatLng(-42.7683, -65.0300), Categoria.AGENCIA),
        Lugar("Ryans", LatLng(-42.7702, -65.0365), Categoria.AGENCIA),
        Lugar("Sentir Patagonia", LatLng(-42.7696, -65.0425), Categoria.AGENCIA),
        Lugar("Sussanich Turismo", LatLng(-42.7710, -65.0373), Categoria.AGENCIA),
        Lugar("Turismo Puma", LatLng(-42.7734, -65.0336), Categoria.AGENCIA),
        Lugar("Ualan Tour", LatLng(-42.7691, -65.0324), Categoria.AGENCIA),
        Lugar("Voz del Mar", LatLng(-42.7715, -65.0360), Categoria.AGENCIA),
        Lugar("Waira", LatLng(-42.7700, -65.0357), Categoria.AGENCIA),
        )

    private fun mostrarLugares(lugares: List<Lugar>) {
        marcadoresVisibles.forEach { it.remove() }
        marcadoresVisibles.clear()

        lugares.forEach { lugar ->
            val marker = googleMap.addMarker(
                MarkerOptions().position(lugar.ubicacion).title(lugar.nombre)
            )
            marker?.let { marcadoresVisibles.add(it) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filtroViewModel = ViewModelProvider(requireActivity())[FiltroViewModel::class.java]

        filtroViewModel.filtroSeleccionado.observe(viewLifecycleOwner) { categoria ->
            if (categoria != null) {
                mostrarLugares(todosLosLugares.filter { it.categoria == categoria })
            } else {
                mostrarLugares(todosLosLugares)
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}
