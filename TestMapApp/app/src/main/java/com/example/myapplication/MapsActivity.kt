package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude = 40.0
    private var longitude = 100.0
    private var zoomStart = 0.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        requestPermissionLauncher.launch(arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.ACCESS_FINE_LOCATION))
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestMultiplePermissions()
        ) { permissions ->
            permissions.entries.forEach{
                val permissionName = it.key
                val isGranted = it.value
                if(isGranted){
                    // Permission granted
                }else{
                    // Permission denied
                }
            }
        }

    private fun getLastKnownLocation(){
        fusedLocationClient.lastLocation.addOnSuccessListener { location->
            if(location != null){
                latitude = location.latitude
                longitude = location.longitude
                zoomStart = 15.0f
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //fusedLocationClient.lastLocation
        // Add a marker in Sydney and move the camera
        //getLastKnownLocation()
        val currLocation = LatLng(latitude,longitude)
        mMap.addMarker(MarkerOptions().position(currLocation).title("Marker at Current Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currLocation))
        mMap.animateCamera(CameraUpdateFactory.zoomTo(zoomStart))
    }
}