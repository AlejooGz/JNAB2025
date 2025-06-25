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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
        Lugar("Hotel Piren", LatLng(-42.768050690693606, -65.03227527534357), Categoria.HOSPEDAJE),
        Lugar("Hotel Península", LatLng(-42.76511063533343, -65.03402387505048), Categoria.HOSPEDAJE),
        Lugar("Hotel Yene Hue", LatLng(-42.76415524356521, -65.03475588138787), Categoria.HOSPEDAJE),
        Lugar("Hotel La Posada", LatLng(-42.78824245117486, -65.01606390535281), Categoria.HOSPEDAJE),
        Lugar("Hotel Tandil", LatLng(-42.77442101434111, -65.03870111977193), Categoria.HOSPEDAJE),
        Lugar("Hostel La Tosca", LatLng(-42.77049563496801, -65.0376193796195), Categoria.HOSPEDAJE),
        Lugar("Complejo El Puente", LatLng(-42.772002881077995, -65.03039796874734), Categoria.HOSPEDAJE),
        Lugar("Complejo Aires del Mar", LatLng(-42.77902560409103, -65.03388063500918), Categoria.HOSPEDAJE),
        Lugar("Complejo Las Tablas", LatLng(-42.78830576263281, -65.01609980383854), Categoria.HOSPEDAJE),
        Lugar("Complejo Manu", LatLng(-42.78633730854853, -65.0256868723431), Categoria.HOSPEDAJE),
        Lugar("Complejo Quilimbai", LatLng(-42.78216016167948, -65.02036484621368), Categoria.HOSPEDAJE),
        Lugar("Complejo Plas Hedd", LatLng(-42.781453543620245, -65.03076989039343), Categoria.HOSPEDAJE),
        Lugar("La Casa del Pino", LatLng(-42.780613155612315, -65.02113641048886), Categoria.HOSPEDAJE),
        Lugar("Abedules Madryn", LatLng(-42.77448247816754, -65.02976812156446), Categoria.HOSPEDAJE),
        Lugar("Las Anémonas", LatLng(-42.77044670988224, -65.03284192794517), Categoria.HOSPEDAJE),
        Lugar("Los Buenos Días", LatLng(-42.78079381232904, -65.02767919486291), Categoria.HOSPEDAJE),
        Lugar("Mar y Arena", LatLng(-42.776304331060295, -65.04237838155213), Categoria.HOSPEDAJE),
        Lugar("AB Alojamientos", LatLng(-42.780320379611126, -65.02319241813962), Categoria.HOSPEDAJE),
        Lugar("Abadía", LatLng(-42.77108687258377, -65.03077443168524), Categoria.HOSPEDAJE),
        Lugar("Arenas de la Patagonia", LatLng(-42.773879864890944, -65.04403162118493), Categoria.HOSPEDAJE),
        Lugar("Cabañas Talismán Azul", LatLng(-42.7857668671528, -65.0239507966966), Categoria.HOSPEDAJE),
        Lugar("Cabañas El Jacaranda", LatLng(-42.78372639843948, -65.0194826936995), Categoria.HOSPEDAJE),
        Lugar("Bistró de Mar", LatLng(-42.7723657833891, -65.02766469394666), Categoria.RESTAURANTE),
        Lugar("Parrilla Big David", LatLng(-42.77256277056446, -65.02798911664559), Categoria.RESTAURANTE),
        Lugar("Fervor", LatLng(-42.7618593905736, -65.03628094820698), Categoria.RESTAURANTE),
        Lugar("All Península", LatLng(-42.76565025445565, -65.03385461227815), Categoria.AGENCIA),
        Lugar("Argentina Visión", LatLng(-42.769021992821116, -65.03126665486892), Categoria.AGENCIA),
        Lugar("Arrieros Patagónicos", LatLng(-42.767852149183334, -65.03240348117833), Categoria.AGENCIA),
        Lugar("Animal Travel", LatLng(-42.767218399027165, -65.03273229811296), Categoria.AGENCIA),
        Lugar("Cuyun Co", LatLng(-42.76498740894729, -65.03418470245165), Categoria.AGENCIA),
        Lugar("Catalejo", LatLng(-42.770018318548104, -65.03095340389052), Categoria.AGENCIA),
        Lugar("Categoría Patagonia", LatLng(-42.76659940605894, -65.03314376591223), Categoria.AGENCIA),
        Lugar("Cerac Travel", LatLng(-42.76510926826034, -65.04069440699472), Categoria.AGENCIA),
        Lugar("Chucao", LatLng(-42.770644950097015, -65.03190087589877), Categoria.AGENCIA),
        Lugar("El Pedral", LatLng(-42.76437204490946, -65.03462444538802), Categoria.AGENCIA),
        Lugar("Explore Patagonia", LatLng(-42.77565465843561, -65.0262398461291), Categoria.AGENCIA),
        Lugar("El Médano", LatLng(-42.76766304617012, -65.03422078386663), Categoria.AGENCIA),
        Lugar("Euforia", LatLng(-42.77050491253195, -65.0331191171963), Categoria.AGENCIA),
        Lugar("Flamenco Tour", LatLng(-42.76585741797337, -65.0411138274327), Categoria.AGENCIA),
        Lugar("Flores Viajes", LatLng(-42.762536299227456, -65.03569444268203), Categoria.AGENCIA),
        Lugar("Forastero Tour", LatLng(-42.768820722813906, -65.03617152904425), Categoria.AGENCIA),
        Lugar("Fugutours", LatLng(-42.76850840732442, -65.03194443029281), Categoria.AGENCIA),
        Lugar("Julidays", LatLng(-42.76106301796615, -65.03917499213135), Categoria.AGENCIA),
        Lugar("Huinca Travel", LatLng(-42.77154021544821, -65.03128495858046), Categoria.AGENCIA),
        Lugar("Montaner", LatLng(-42.761872222439926, -65.03738841953836), Categoria.AGENCIA),
        Lugar("Odisea Patagónica", LatLng(-42.76717498845028, -65.03279057212563), Categoria.AGENCIA),
        Lugar("Punta Tombo Turismo", LatLng(-42.78475195629754, -65.0093262820395), Categoria.AGENCIA),
        Lugar("Rústica", LatLng(-42.762489303921605, -65.03588909067422), Categoria.AGENCIA),
        Lugar("Ryans", LatLng(-42.76282328665981, -65.03901821554582), Categoria.AGENCIA),
        Lugar("Sentir Patagonia", LatLng(-42.775447848971154, -65.0267163197977), Categoria.AGENCIA),
        Lugar("Sussanich Turismo", LatLng(-42.770877879848406, -65.03741234498274), Categoria.AGENCIA),
        Lugar("Turismo Puma", LatLng(-42.764414813088244, -65.0345931916764), Categoria.AGENCIA),
        Lugar("Ualan Tour", LatLng(-42.77167612579615, -65.0294036030915), Categoria.AGENCIA),
        Lugar("Voz del Mar", LatLng(-42.7676068682462, -65.04071956391002), Categoria.AGENCIA),
        Lugar("Waira", LatLng(-42.766931684978495, -65.03911004199881), Categoria.AGENCIA),
    )

    private fun mostrarLugares(lugares: List<Lugar>) {
        marcadoresVisibles.forEach { it.remove() }
        marcadoresVisibles.clear()

        lugares.forEach { lugar ->
            val color = when (lugar.categoria) {
                Categoria.HOSPEDAJE -> BitmapDescriptorFactory.HUE_MAGENTA
                Categoria.RESTAURANTE -> BitmapDescriptorFactory.HUE_ORANGE
                Categoria.AGENCIA -> BitmapDescriptorFactory.HUE_CYAN
            }

            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(lugar.ubicacion)
                    .title(lugar.nombre)
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
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

        filtroViewModel.filtrosSeleccionados.observe(viewLifecycleOwner) { categorias ->
            if (::todosLosLugares.isInitialized) {
                if (categorias.isEmpty()) {
                    mostrarLugares(todosLosLugares)
                } else {
                    mostrarLugares(todosLosLugares.filter { it.categoria in categorias })
                }
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}
