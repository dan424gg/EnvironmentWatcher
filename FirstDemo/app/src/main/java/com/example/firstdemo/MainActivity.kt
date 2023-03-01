package com.example.firstdemo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdemo.Location.LocationClass
import com.example.firstdemo.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var lastLocation = LatLng(0.0, 0.0)
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding

    private var latitude : Double = 0.0
    private var longitude : Double = 0.0
    private var curLocation : LatLng = LatLng(latitude, longitude)

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        var path : MutableList<List<LatLng>> = ArrayList()
        mMap = googleMap

        val notificationIntent = Intent(this, NotificationActivity::class.java)

        thread {

            // Runs only when Button is True
            while (true) {
                curLocation = LocationClass.calling(this)
                Thread.sleep(1000)

                if (lastLocation != curLocation) {
                    lastLocation = curLocation

                    val origin = curLocation
                    val destination = LatLng(46.8802, -117.3643)

                    runOnUiThread {
                        // Creating LatLngBounds obj to create a "bounds" for what is displayed on the map
                        val routeBounds = LatLngBounds.builder()
                        routeBounds.include(origin).include(destination)

                        mMap.addMarker(MarkerOptions().position(origin).title("Origin"))
                        mMap.addMarker(MarkerOptions().position(destination).title("Destination"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(routeBounds.build(), 1000, 1000, 0))

                        RoutingClass.calling(googleMap, origin, destination, this)
                    }
                }
            }
        }
    }

    // Change weather display icon on map
    private fun displayWeather(weather: String) {
        Log.d("DEBUG", "In displayWeather")
        val weatherImage : ImageView = findViewById(R.id.weatherImage)
        val badWeather = mapOf("cloudy" to R.drawable.cloudy, "sunny" to R.drawable.sunny,
            "rain" to R.drawable.rain, "clear" to R.drawable.sunny)
        var badWeatherExists = false

        // Check if forecast is in list of weather types
        for (weatherType in badWeather.keys) {
            if (weather.contains(weatherType, ignoreCase = true)) {
                badWeather[weatherType]?.let {
                    badWeatherExists = true
                    weatherImage.setImageResource(it) }
                break
            }
        }

        // Dangerous weather not detected
        if (!badWeatherExists) {
            // icon default is lightning for testing
            weatherImage.setImageResource(R.drawable.lightning)
        }
    }
}
