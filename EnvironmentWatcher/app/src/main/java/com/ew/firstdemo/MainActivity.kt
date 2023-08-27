package com.ew.firstdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
//import com.ew.firstdemo.Location.RoutingClass
import com.ew.firstdemo.Weather.WeatherParser
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private var createdRoute = false
    val viewModel by viewModels<MainActivityViewModel>()

    @SuppressLint("MissingPermission", "UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen().apply {
            this.setKeepOnScreenCondition {
                viewModel.locationState.value.isLoading
            }
        }

        // Make sure the app was granted the needed permissions
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Get permissions if needed
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        }

        setContentView(R.layout.activity_main)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // initialize button variables
        val navSwitch: SwitchCompat = findViewById(R.id.navigation)
        val dirButton: Button = findViewById(R.id.dirButton)

        // get array of cities for autocomplete
        val cities = resources.getStringArray(R.array.USCities)

        // Get the inputs from the text fields
        val startLocationInput = findViewById<AutoCompleteTextView>(R.id.startLocation)
        val destLocationInput = findViewById<AutoCompleteTextView>(R.id.endLocation)

        // Autocompletes text user inputs in source and destination fields
        setupAutoComplete(startLocationInput, cities)
        setupAutoComplete(destLocationInput, cities)

        // When an autocomplete choice is picked, hide keyboard
        startLocationInput.setOnDismissListener {
            hideKeyboard()
        }

        // When an autocomplete choice is picked, hide keyboard
        destLocationInput.setOnDismissListener {
            hideKeyboard()
        }

        // Find directions when the button is pushed
        dirButton.setOnClickListener {
            lifecycleScope.launch {
                viewModel.getRoute(
                    startLocationInput.text.toString(), destLocationInput.text.toString()
                )

//                if (state.error != null) {
//                    Snackbar.make(
//                        findViewById(R.id.mainView), state.error!!, Snackbar.LENGTH_SHORT
//                    ).show()
//                    return@launch
//                }
//
//                val route = state.route!!
//                mMap.clear()
//                mMap.addPolyline(
//                    PolylineOptions().addAll(route.path!!).color(
//                        android.graphics.Color.BLUE
//                    )
//                )
//
//                for (location in route.routeInfo!!) {
//                    val locIcon = Bitmap.createScaledBitmap(
//                        WeatherParser(
//                            location.weather!!, this@MainActivity
//                        ).img, 150, 150, false
//                    )
//                    mMap.addMarker(
//                        MarkerOptions().position(location.coordinates!!).title(location.weather!!)
//                            .icon(
//                                BitmapDescriptorFactory.fromBitmap(locIcon)
//                            ).anchor(0.5f, 0.5f)
//                    )
//                }
//
//                val routeBounds = LatLngBounds.builder()
//                routeBounds.include(route.routeInfo?.first()?.coordinates!!)
//                    .include(route.routeInfo?.last()?.coordinates!!)
//                mMap.moveCamera(
//                    CameraUpdateFactory.newLatLngBounds(
//                        routeBounds.build(), 1000, 1000, 0
//                    )
//                )
            }
        }

        navSwitch.setOnClickListener {
            if (navSwitch.isChecked) {
                createdRoute = true
                startLocationInput.visibility = View.VISIBLE
                destLocationInput.visibility = View.VISIBLE
                dirButton.visibility = View.VISIBLE
            } else {
                createdRoute = false
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
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        // Set the global mMap variable to point to the now initialized googleMap
        mMap = googleMap
        mMap.setOnMapClickListener(this)

        lifecycleScope.launch {
            viewModel.locationState.collect {
                if (!createdRoute && !viewModel.locationState.value.isLoading) {
                    val state = viewModel.locationState.value.curLocation!!
                    val userIcon = Bitmap.createScaledBitmap(
                        WeatherParser(state.weather!!, this@MainActivity).img, 150, 150, false
                    )
                    mMap.clear()
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.addMarker(
                        MarkerOptions().position(state.coordinates!!).icon(
                            BitmapDescriptorFactory.fromBitmap(userIcon)
                        ).anchor(0.5f, 0.5f).title("Current Location")
                    )!!
                    mMap.moveCamera(CameraUpdateFactory.zoomTo(12f))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(state.coordinates!!))
                } else if (!viewModel.locationState.value.isLoading) {
                    if (it.error != null) {
                        Snackbar.make(
                            findViewById(R.id.mainView), it.error!!, Snackbar.LENGTH_SHORT
                        ).show()
                        return@collect
                    }

                    val route = it.route!!
                    mMap.clear()
                    mMap.addPolyline(
                        PolylineOptions().addAll(route.path!!).color(
                            android.graphics.Color.BLUE
                        )
                    )

                    for (location in route.routeInfo!!) {
                        val locIcon = Bitmap.createScaledBitmap(
                            WeatherParser(
                                location.weather!!, this@MainActivity
                            ).img, 150, 150, false
                        )
                        mMap.addMarker(
                            MarkerOptions().position(location.coordinates!!).title(location.weather!!)
                                .icon(
                                    BitmapDescriptorFactory.fromBitmap(locIcon)
                                ).anchor(0.5f, 0.5f)
                        )
                    }

                    val routeBounds = LatLngBounds.builder()
                    routeBounds.include(route.path!!.first())
                        .include(route.path!!.last())
                    mMap.moveCamera(
                        CameraUpdateFactory.newLatLngBounds(
                            routeBounds.build(), 1000, 1000, 0
                        )
                    )
                }
            }
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (!createdRoute) {
                    Log.d("test", "updated location")
                    viewModel.getCurrentLocationInfo()
                    delay(10000L)
                }
            }
        }
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
//        // Make sure that the app has permission to access the user's permissions
//        if (ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
//                this, Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//
//            // If not, get them
//            ActivityCompat.requestPermissions(
//                this, arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ), 1
//            )
//
//        } else {
//            // Otherwise, enable the location features
//            mMap.isMyLocationEnabled = true
//            mMap.uiSettings.isMyLocationButtonEnabled = true
//        }
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
