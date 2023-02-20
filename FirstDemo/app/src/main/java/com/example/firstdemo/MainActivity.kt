package com.example.firstdemo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdemo.Location.LocationClass
import com.example.firstdemo.Weather.WeatherClass
import com.example.firstdemo.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var lastLocation: Pair<Double, Double>? = null
    private lateinit var lastWeather: String
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private var runThread = true

    
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        /*
        val lat: TextView = findViewById(R.id.latitude)
        val long: TextView = findViewById(R.id.longitude)
        val curWeather: TextView = findViewById(R.id.weather)
        val locationButton: Button = findViewById(R.id.loc_button)
        val weatherButton: Button = findViewById(R.id.weather_button)

         */

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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

        val notificationIntent = Intent(this, NotificationActivity::class.java)

        /*
        locationButton.setOnClickListener{
            runThread = !runThread
        }

         */

        Thread(Runnable {
            // Runs only when Button is True
            while (runThread) {
                Log.d("DEBUG","Entered thread")
                val (p_lat, p_long) = LocationClass.calling(this)

                val location = Pair(p_lat, p_long)

                Thread.sleep(2500)
                Log.d("DEBUG","Entered thread 2")
                if(lastLocation != location) {
                    lastLocation = location
                    val latLng = LatLng(location.first, location.second)
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

                    // Zoom in further

                    mMap.moveCamera(CameraUpdateFactory.zoomTo(13f))

                    val weather = WeatherClass.calling(location.first, location.second)
                    // Update weather
                    if (weather != lastWeather) {
                        lastWeather = weather

                        displayWeather(weather)
                    }
                }
                Thread.sleep(2500)
            }
        }).start()

        /*
        Thread(Runnable {
            // Runs only when Button is True
            while (runThread) {
                Thread.sleep(2500)
                val forecast = WeatherClass.calling(latitude, longitude)
                runOnUiThread {
                    curWeather.text = forecast
                }
                Thread.sleep(2500)
            }
        }).start()
         */
        /*
            weatherButton.setOnClickListener{
                runThread = !runThread
            }

         */



    }

    // Change weather display icon on map
    private fun displayWeather(weather: String) {
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
