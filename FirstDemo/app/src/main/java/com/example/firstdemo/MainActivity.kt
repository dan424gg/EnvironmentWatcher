package com.example.firstdemo

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.Address
import android.location.Geocoder.GeocodeListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
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
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    var changeViewToCurLocation = true
    private var curLocation = LatLng(0.0, 0.0)


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationClass.makeNotificationChannel(
            this, "default", getString(R.string.WeatherUpdateChannelName),
            "Current weather", 3
        )

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        val dirButton: Button = findViewById(R.id.dirButton)
        dirButton.setOnClickListener{

            Log.d("debug", "button clicked")

            changeViewToCurLocation = false

            val startLocationInput = findViewById<EditText>(R.id.startLocation)
            val endLocationInput = findViewById<EditText>(R.id.endLocation)
            var origin = curLocation
            var destination = LatLng(0.0, 0.0)

            if (startLocationInput.text.isNotEmpty()) {
                origin = locNameToLatLng(startLocationInput.text.toString())
            }

            if (endLocationInput.text.isNotEmpty()) {
                destination = locNameToLatLng(endLocationInput.text.toString())
            }

            // Creating LatLngBounds obj to create a "bounds" for what is displayed on the map
            if (origin != LatLng(0.0, 0.0) && destination != LatLng(0.0, 0.0)) {
                val routeBounds = LatLngBounds.builder()
                routeBounds.include(origin).include(destination)

                mMap.clear()
                mMap.addMarker(MarkerOptions().position(origin).title("Origin"))
                mMap.addMarker(MarkerOptions().position(destination).title("Destination"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(routeBounds.build(), 1000, 1000, 0))

                RoutingClass.calling(mMap, origin, destination, this)
            }
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
            val settingsIntent = Intent(this, SettingsActivity::class.java)
            startActivity(settingsIntent)
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val icon = BitmapFactory.decodeResource(resources, R.drawable.user_icon)
        Bitmap.createScaledBitmap(icon, 120, 120, false)

        Thread {
            // Runs only when Button is True
            while (true) {
                curLocation = LocationClass.calling(this)

                // Make sure the location is valid
                if(curLocation != LatLng(0.0,0.0)) {
                    WeatherClass.getWeatherData(curLocation, 0, "shortForecast") { weather ->

                        val userIcon =
                            Bitmap.createScaledBitmap(getWeatherImage(weather), 150, 150, false)

                        //call notification for test
                        NotificationClass.sendNotification(this, weather, weather, userIcon)

                        runOnUiThread {
                            if (changeViewToCurLocation) {
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation))
                                mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))

                                mMap.addMarker(
                                    MarkerOptions().position(curLocation).icon(
                                        BitmapDescriptorFactory.fromBitmap(userIcon)
                                    )
                                )

                                changeViewToCurLocation = false
                            }
                        }
                    }
                }
            }
        }.start()
    }

    private fun locNameToLatLng(loc : String) : LatLng {

        val geocoder = Geocoder(this)
        val addressList = geocoder.getFromLocationName(loc, 1)

        if (addressList != null) {
            if (addressList.isNotEmpty()) {
                return LatLng(addressList[0].latitude, addressList[0].longitude)
            } else {
                // Handle case where no results were found
                Log.d("DEBUG", "AddressList is not empty!")
            }
        }

        return LatLng(0.0, 0.0)
    }

    // Change weather display icon on map
    fun getWeatherImage(curWeather: String): Bitmap {
        lateinit var bitmap: Bitmap

        val weatherImage: ImageView = findViewById(R.id.weatherImage)
        val badWeather = mapOf(
            "cloudy" to R.drawable.cloudy, "sunny" to R.drawable.sunny,
            "rain" to R.drawable.rain, "clear" to R.drawable.sunny
        )
        var badWeatherExists = false

        // Check if forecast is in list of weather types
        for (weatherType in badWeather.keys) {
            if (curWeather.contains(weatherType, ignoreCase = true)) {
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
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.lightning)
        }

        return bitmap
    }

}
