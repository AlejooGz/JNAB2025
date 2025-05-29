package com.example.jnab2025.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.jnab2025.R

class MapsFragment : Fragment() {

    private val callback = OnMapReadyCallback { googleMap ->
        // Centro en Puerto Madryn
        val madryn = LatLng(-42.7692, -65.0385)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madryn, 14f))

        // Ejemplo de lugares con descuentos
        val lugares = listOf(
            Pair("Hotel Piren", LatLng(-42.7729, -65.0331)),
            Pair("Hotel Península", LatLng(-42.7734, -65.0320)),
            Pair("Parrilla Big David", LatLng(-42.7720, -65.0405)),
            Pair("Restaurante Bistró de Mar", LatLng(-42.7745, -65.0241)),
            Pair("All Península Agencia", LatLng(-42.7725, -65.0329)),
            Pair("Hostel La Tosca", LatLng(-42.7712, -65.0375))
        )

        for ((nombre, ubicacion) in lugares) {
            googleMap.addMarker(MarkerOptions().position(ubicacion).title(nombre))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_maps, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
}