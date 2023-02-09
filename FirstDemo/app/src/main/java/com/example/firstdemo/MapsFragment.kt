package com.example.firstdemo

import android.R.attr.defaultValue
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapsFragment : Fragment() {

    private var latitude = 0.0
    private var longitude = 0.0

    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        val currLoc = LatLng(latitude, longitude)
        Log.d("DEBUG", "MAP CALLBACK")
        googleMap.addMarker(MarkerOptions().position(currLoc).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(currLoc))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bundle = this.arguments
        if (bundle != null) {
            latitude = bundle.getDouble("latitude", latitude)
            longitude = bundle.getDouble("longitude", longitude)
        }
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        Log.d("DEBUG", "CREATED MAP VIEW")
    }
}