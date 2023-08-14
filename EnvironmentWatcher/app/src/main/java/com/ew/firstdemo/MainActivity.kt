package com.ew.firstdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ew.firstdemo.Location.CurrentLocationViewModel
import com.ew.firstdemo.Location.NameToCoordinates
import com.ew.firstdemo.Location.RoutingClass
import com.ew.firstdemo.Weather.WeatherClass
import com.ew.firstdemo.Weather.WeatherParser
import com.ew.firstdemo.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private var createdRoute = false
    val viewModel by viewModels<CurrentLocationViewModel>()

    @SuppressLint("MissingPermission", "UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize the app
        super.onCreate(savedInstanceState)
        viewModel.getLocation(this)

        /* initialize splash screen */
        installSplashScreen().apply {
            this.setKeepOnScreenCondition {
                viewModel.isLoading.value!!
            }
        }

        setContentView(R.layout.activity_main)

        // Create the channel to allow utilize notifications
//        NotificationClass.makeNotificationChannel(
//            this, "default", getString(R.string.WeatherUpdateChannelName), "Current weather", 3
//        )

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // start and destination coords placeholders
        lateinit var start: LatLng
        lateinit var destination: LatLng

        // Get the inputs from the text fields
        val startLocationInput = findViewById<AutoCompleteTextView>(R.id.startLocation)
        val destLocationInput = findViewById<AutoCompleteTextView>(R.id.endLocation)

        // initialize button variables
        val navSwitch: SwitchCompat = findViewById(R.id.navigation)
        val dirButton: Button = findViewById(R.id.dirButton)

        // handle light/night time configuration
        when (applicationContext.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)) {
            Configuration.UI_MODE_NIGHT_YES -> run {
                startLocationInput.background = getDrawable(R.drawable.edit_text_bg_night)
                destLocationInput.background = getDrawable(R.drawable.edit_text_bg_night)
                navSwitch.background = getDrawable(R.drawable.edit_text_bg_night)
            }

            else -> run {

                startLocationInput.background = getDrawable(R.drawable.edit_text_background)
                destLocationInput.background = getDrawable(R.drawable.edit_text_background)
                navSwitch.background = getDrawable(R.drawable.edit_text_background)
            }
        }

        // get array of cities for autocomplete
        val cities = resources.getStringArray(R.array.USCities)

        // Autocompletes text user inputs in source and destination fields
        setupAutoComplete(startLocationInput, cities)
        setupAutoComplete(destLocationInput, cities)

        // When a autocomplete choice is picked, hide keyboard
        startLocationInput.setOnDismissListener {
            hideKeyboard()
        }

        // When a autocomplete choice is picked, hide keyboard
        destLocationInput.setOnDismissListener {
            hideKeyboard()
        }

        // Find directions when the button is pushed
        dirButton.setOnClickListener {

            // Set destination coordinates
            if (destLocationInput.text.toString() == "") {
                // Show pop-up saying that destination is required
                Snackbar.make(
                    findViewById(R.id.mainView), "A destination is required!", Snackbar.LENGTH_SHORT
                ).show()
            } else {
                Log.d("hail", startLocationInput.text.toString())
                NameToCoordinates.getCityCoords(
                    startLocationInput.text.toString(), destLocationInput.text.toString(), this
                ) { coords ->
                    start = coords.first!!
                    destination = coords.second

                    if (start == LatLng(1.0, 1.0)) {
                        Snackbar.make(
                            findViewById(R.id.mainView),
                            "The start location that was supplied is not applicable!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else if (destination == LatLng(1.0, 1.0)) {
                        Snackbar.make(
                            findViewById(R.id.mainView),
                            "The destination that was supplied is not applicable!",
                            Snackbar.LENGTH_SHORT
                        ).show()
                    } else {
                        hideKeyboard()

                        runOnUiThread {

                            createdRoute = true

                            mMap.clear()

                            // Set the bounds of the route to the start and the destination
                            val routeBounds = LatLngBounds.builder()
                            routeBounds.include(start).include(destination)
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngBounds(
                                    routeBounds.build(), 1000, 1000, 0
                                )
                            )

                            // based on weather data for the destination, display it on the map
                            WeatherClass.getWeatherData(
                                destination, 0, "shortForecast"
                            ) { weather ->

                                // Find the weather at the destination
                                val destIcon = Bitmap.createScaledBitmap(
                                    WeatherParser(weather, this).img, 150, 150, false
                                )

                                runOnUiThread {
                                    mMap.addMarker(
                                        MarkerOptions().position(destination).icon(
                                            BitmapDescriptorFactory.fromBitmap(destIcon)
                                        ).anchor(0.5f, 0.5f).title("Destination")
                                    )!!
                                }
                            }
                        }

                        // Calculate the optimal route for the user's requested directions
                        RoutingClass.calling(mMap, start, destination, this)
                    }
                }
            }
        }

        navSwitch.setOnClickListener {
            if (navSwitch.isChecked) {
                startLocationInput.visibility = View.VISIBLE
                destLocationInput.visibility = View.VISIBLE
                dirButton.visibility = View.VISIBLE
            } else {
                startLocationInput.visibility = View.INVISIBLE
                destLocationInput.visibility = View.INVISIBLE
                dirButton.visibility = View.INVISIBLE
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    // Initialize map menu on UI
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.map_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    // Handle menu item selection
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if (item.itemId == R.id.settingsOption) {
//            val settingsIntent = Intent(this, SettingsActivity::class.java)
//            startActivity(settingsIntent)
//        } else
        if (item.itemId == R.id.aboutOption) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://environment-watcher-61187.web.app/")
                )
            )
        }
        return true
    }

    // allows map to be clickable
    override fun onMapClick(p0: LatLng) {
        hideKeyboard()
    }

    // Performs operations related to the map once it is ready
    override fun onMapReady(googleMap: GoogleMap) {
        // Set the global mMap variable to point to the now initialized googleMap
        mMap = googleMap
        mMap.setOnMapClickListener(this)

        viewModel.curLocation.observe(this) { curLocation ->
            if (!viewModel.isLoading.value!!) {
                WeatherClass.getWeatherData(curLocation!!, 0, "shortForecast") { weather ->

                    // Find the weather icon corresponding to the user's current location to use
                    // as the image for the user marker
                    val userIcon = Bitmap.createScaledBitmap(
                        WeatherParser(weather, this).img, 150, 150, false
                    )

                    runOnUiThread {
                        mMap.addMarker(
                            MarkerOptions().position(curLocation!!).icon(
                                BitmapDescriptorFactory.fromBitmap(userIcon)
                            ).anchor(0.5f, 0.5f).title("Current Location")
                        )!!
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation!!))
                    }
                }
            }
        }

        // Request permissions and enable the button to recenter the map
        enableLocationButton()

        // Use a thread so that other processes do not have to wait for the location or weather
        val executor = Executors.newSingleThreadScheduledExecutor()

        // Use a thread so that other processes do not have to wait for the location or weather
        executor.scheduleAtFixedRate(
            {
                viewModel.getLocation(this)
//                if (curLocation != LatLng(0.0, 0.0)) {
//
//                    // Get the weather using the shortForecast and continue to the rest of the operations
//                    WeatherClass.getWeatherData(curLocation!!, 0, "shortForecast") { weather ->
//
//                        // Find the weather icon corresponding to the user's current location to use
//                        // as the image for the user marker
//                        val userIcon = Bitmap.createScaledBitmap(
//                            WeatherParser(weather, this).img, 150, 150, false
//                        )
//
//                        runOnUiThread {
//                            /* if the route isn't created, follow user's current location and
//                             * update weather if needed */
//                            if (!createdRoute) {
//                                mMap.clear()
//                                mMap.addMarker(
//                                    MarkerOptions().position(curLocation!!).icon(
//                                        BitmapDescriptorFactory.fromBitmap(userIcon)
//                                    ).anchor(0.5f, 0.5f).title("Current Location")
//                                )!!
//                                mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))
//                                mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation!!))
//                            }
//                        }
//                    }
//                }
            }, 0, 10, TimeUnit.SECONDS
        )

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setupAutoComplete(editTextCity: AutoCompleteTextView, cities: Array<String>) {
        // Used to update list of autocomplete cities
        val adapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, cities
        )
        editTextCity.setAdapter(adapter)
        editTextCity.threshold = 1
    }

    // Function to get permission to let google maps access the user's location and enable
    // the recenter button
    @SuppressLint("MissingPermission")
    private fun enableLocationButton() {
        // Make sure that the app has permission to access the user's permissions
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            // If not, get them
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )

        } else {
            // Otherwise, enable the location features
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        }
    }

    // Function that is called when user grants app permission
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            // Got permission from user
            enableLocationButton()
        } else {
            Log.d("hail", "user didn't give location permission!")
        }
    }
}
