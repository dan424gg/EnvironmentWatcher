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
import com.google.android.gms.maps.model.MarkerOptions

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var lastLocation = Pair(0.0, 0.0)
    private var lastWeather = "Forecast goes here!"
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    var moveCam = true
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
            moveCam = true
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
            // Call settings activity
            Log.d("DEBUG","Settings clicked")
        }
        return true
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
        var moveCam = true
        mMap.setOnCameraMoveStartedListener {
            //moveCam = false
        }

/*        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
        var icon = BitmapFactory.decodeResource(resources, R.drawable.user_icon)

        icon = Bitmap.createScaledBitmap(icon, 120, 120, false)
        //mMap.uiSettings.isZoomControlsEnabled = true

        val notificationIntent = Intent(this, NotificationActivity::class.java)
        val weatherImage : ImageView = findViewById(R.id.weatherImage)

        Thread {
            // Runs only when Button is True
            while (true) {
                Log.d("DEBUG", "Entered thread")
                curLocation = LocationClass.calling(this)
                Log.d("DEBUG", "Location: $curLocation")
                val latLng = LatLng(curLocation.latitude, curLocation.longitude)
                /*
                if (lastLocation != location) {
                    Log.d("DEBUG", "$lastLocation, $location")
                    lastLocation = location
                    val latLng = LatLng(location.first, location.second)

                    runOnUiThread {
                        Log.d("DEBUG", "Updating map location")

                        mMap.addMarker(MarkerOptions().position(LatLng(p_lat, p_long)).icon(
                            BitmapDescriptorFactory.fromBitmap(icon)))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))

                        // Zoom in further
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))
                    }
                }

                 */
                Thread.sleep(500)

                if(curLocation.latitude != 0.0) { // Make sure the location is not outside of the US
                    Log.d("DEBUG", "Inside weather")
                    weather = WeatherClass.getWeatherData(curLocation)
                    Log.d("DEBUG", "weather: $weather")
                    var userIcon = Bitmap.createScaledBitmap(getWeatherImage(weather), 150, 150, false)
                    runOnUiThread {
                        if(moveCam){
                            Log.d("DEBUG", "Camera update")
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))
                            moveCam = false
                        }
                        mMap.addMarker(
                            MarkerOptions().position(latLng).icon(
                                BitmapDescriptorFactory.fromBitmap(userIcon)
                            )
                        )
                    }
                }
                Thread.sleep(500)
                // Update weather
//                 if (weather != lastWeather) {
//                     Log.d("DEBUG", "Weather update: $weather")
//                     lastWeather = weather
//                     runOnUiThread {
//                         displayWeather(weather)
//                     }
//                 }
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
            //weatherImage.setImageResource(R.drawable.lightning)
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.lightning)
        }

        return bitmap
    }

}
