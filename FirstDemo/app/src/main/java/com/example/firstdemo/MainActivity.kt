package com.example.firstdemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.firstdemo.Location.LocationClass
import com.example.firstdemo.Weather.WeatherClass
import com.example.firstdemo.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

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
            Log.d("DEBUG", "Directions clicked")
            changeViewToCurLocation = false

            val startLocationInput = findViewById<EditText>(R.id.startLocation)
            val endLocationInput = findViewById<EditText>(R.id.endLocation)
            var start = curLocation
            var destination = LatLng(0.0, 0.0)

            Log.d("DEBUG", "Start: ${startLocationInput.text.toString()}")
            Log.d("DEBUG", "Destination: ${endLocationInput.text.toString()}")

            if (startLocationInput.text != null) {
                start = locNameToLatLng(startLocationInput.text.toString())
            }

            if (endLocationInput.text != null) {
                destination = locNameToLatLng(endLocationInput.text.toString())
            }

            // Creating LatLngBounds obj to create a "bounds" for what is displayed on the map
            if (destination != LatLng(0.0, 0.0) && start != LatLng(0.0, 0.0)) {
                val routeBounds = LatLngBounds.builder()
                routeBounds.include(start).include(destination)

                mMap.addMarker(MarkerOptions().position(start).title("Origin"))
                mMap.addMarker(MarkerOptions().position(destination).title("Destination"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(routeBounds.build(), 1000, 1000, 0))

                RoutingClass.calling(mMap, start, destination, this)
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
        enableLocationButton()

        val notificationIntent = Intent(this, NotificationActivity::class.java)
        val weatherImage : ImageView = findViewById(R.id.weatherImage)
        var markerCreated = false
        lateinit var userMarker: Marker

        Thread {
            // Runs only when Button is True
            while (true) {
                Log.d("DEBUG", "Entered thread")
                curLocation = LocationClass.calling(this, mMap)
                Log.d("DEBUG", "Location: $curLocation")
                Thread.sleep(500)

                if(curLocation.latitude != 0.0) { // Make sure the location is not outside of the US
                    Log.d("DEBUG", "Inside weather")
                    //weather = WeatherClass.getWeatherData(curLocation)
                    WeatherClass.getWeatherData(curLocation, 0, "shortForecast") { weather ->
//

                        Log.d("weatherresult", weather)
                        val userIcon =
                            Bitmap.createScaledBitmap(getWeatherImage(weather), 150, 150, false)

                        //call notification for test
                        NotificationClass.sendNotification(this, weather, weather, userIcon)

                        runOnUiThread {
                            if (changeViewToCurLocation) {
                                Log.d("DEBUG", "Camera update")
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation))
                                mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))
                                changeViewToCurLocation = false
                            }

                            if(!markerCreated) {
                                userMarker = mMap.addMarker(
                                    MarkerOptions().position(curLocation).icon(
                                        BitmapDescriptorFactory.fromBitmap(userIcon)
                                    )
                                )!!
                                markerCreated = true
                            }else if(userMarker.position != curLocation){
                                userMarker.position= curLocation
                                userMarker.setIcon(BitmapDescriptorFactory.fromBitmap(userIcon))

                            }
                        }
                    }
                }
                Thread.sleep(500)
            }
        }.start()
    }

    private fun locNameToLatLng(loc : String) : LatLng {
        val geocoder = Geocoder(this)
        val addressList = geocoder.getFromLocationName(loc, 1)

        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            /*addressList = geocoder.getFromLocationName(startLocStr, 1,
                Geocoder.GeocodeListener())

             */
        } else {
            addressList = geocoder.getFromLocationName(startLocStr, 1)
        }

         */


        if (addressList != null) {
            if (addressList.isNotEmpty()) {
                Log.d("DEBUG", "Address list: $addressList")

                return LatLng(addressList[0].latitude, addressList[0].longitude)
            } else {
                // Handle case where no results were found
            }
        }

        return LatLng(0.0, 0.0)
    }

    // Change weather display icon on map
    fun getWeatherImage(curWeather: String): Bitmap {
        lateinit var bitmap: Bitmap

        Log.d("DEBUG", "In displayWeather: $curWeather")
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
            // weatherImage.setImageResource(R.drawable.lightning)
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.lightning)
        }

        return bitmap
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun enableLocationButton(){
        val client = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1)
        }else{
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1){
            // Got permission from user
            enableLocationButton()
        }
    }

}
