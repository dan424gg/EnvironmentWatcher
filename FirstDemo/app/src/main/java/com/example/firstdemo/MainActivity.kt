package com.example.firstdemo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdemo.Location.LocationClass
import com.example.firstdemo.Weather.WeatherClass
import com.example.firstdemo.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var lastLocation = Pair(0.0, 0.0)
    private var lastWeather = "Forecast goes here!"
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    var changeViewToCurLocation = true
    private lateinit var curLocation: LatLng
    private var weather = "Forecast goes here!"


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

        val dirButton: Button = findViewById(R.id.dirButton)
        dirButton.setOnClickListener{
            Log.d("DEBUG", "Directions clicked")
            changeViewToCurLocation = false

            // Change destination to destination latlng
            val destination = LatLng(46.8802, -117.3643)

            // Creating LatLngBounds obj to create a "bounds" for what is displayed on the map
            val routeBounds = LatLngBounds.builder()
            routeBounds.include(curLocation).include(destination)

            mMap.addMarker(MarkerOptions().position(curLocation).title("Origin"))
            mMap.addMarker(MarkerOptions().position(destination).title("Destination"))
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(routeBounds.build(), 1000, 1000, 0))

            RoutingClass.calling(mMap, curLocation, destination, this)
        }
    }

    // Initialize map menu on UI
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.map_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Handle item selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.settingsOption) {
            Log.d("DEBUG","Settings clicked")
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
            Log.d("DEBUG","Settings clicked")
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val icon = BitmapFactory.decodeResource(resources, R.drawable.user_icon)
        Bitmap.createScaledBitmap(icon, 120, 120, false)

        val notificationIntent = Intent(this, NotificationActivity::class.java)
        val weatherImage : ImageView = findViewById(R.id.weatherImage)

        Thread {
            // Runs only when Button is True
            while (true) {
                Log.d("DEBUG", "Entered thread")
                curLocation = LocationClass.calling(this)
                Log.d("DEBUG", "Location: $curLocation")
                Thread.sleep(500)

                if(curLocation.latitude != 0.0) { // Make sure the location is not outside of the US
                    Log.d("DEBUG", "Inside weather")
                    weather = WeatherClass.getWeatherData(curLocation)
                    Log.d("DEBUG", "weather: $weather")
                    val userIcon = Bitmap.createScaledBitmap(getWeatherImage(weather), 150, 150, false)

                    runOnUiThread {
                        if(changeViewToCurLocation){
                            Log.d("DEBUG", "Camera update")
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation))
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))
                            changeViewToCurLocation = false
                        }

                        mMap.addMarker(
                            MarkerOptions().position(curLocation).icon(
                                BitmapDescriptorFactory.fromBitmap(userIcon)
                            )
                        )
                    }
                }
            }
        }.start()
    }

    // Change weather display icon on map
    private fun getWeatherImage(weather: String): Bitmap {
        lateinit var bitmap: Bitmap

        Log.d("DEBUG", "In displayWeather")
        val weatherImage: ImageView = findViewById(R.id.weatherImage)
        val badWeather = mapOf(
            "cloudy" to R.drawable.cloudy, "sunny" to R.drawable.sunny,
            "rain" to R.drawable.rain, "clear" to R.drawable.sunny
        )
        var badWeatherExists = false

        // Check if forecast is in list of weather types
        for (weatherType in badWeather.keys) {
            if (weather.contains(weatherType, ignoreCase = true)) {
                badWeather[weatherType]?.let {
                    badWeatherExists = true
                    bitmap = BitmapFactory.decodeResource(resources, it)
                }
                break
            }
        }

        // Dangerous weather not detected
        if (!badWeatherExists) {
            // icon default is lightning for testing
            // weatherImage.setImageResource(R.drawable.lightning)
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.lightning)
        }

        return bitmap
    }

}
