package com.example.firstdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.firstdemo.Location.LocationClass
import com.example.firstdemo.Weather.WeatherClass
import com.example.firstdemo.Weather.WeatherParser
import com.example.firstdemo.databinding.ActivityMainBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    //private var lastLocation = Pair(0.0, 0.0)
    //private var lastWeather = "Forecast goes here!"
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMainBinding
    private var changeViewToCurLocation = true
    private lateinit var curLocation: LatLng
    //private var startAddress : Address? = null
    //private lateinit var destAddress : Address
    private var startCitySelected = false
    private var destCitySelected = false
    //private val cities = arrayOf("hi", "bye")

    //private var weather = "Forecast goes here!"


    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        // Initialize the app
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Create the channel to allow utilize notifications
        NotificationClass.makeNotificationChannel(
            this, "default", getString(R.string.WeatherUpdateChannelName),
            "Current weather", 3
        )

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        // Get the inputs from the text fields
        val startLocationInput = findViewById<AutoCompleteTextView>(R.id.startLocation)
        val destLocationInput = findViewById<AutoCompleteTextView>(R.id.endLocation)

        lateinit var start: LatLng
        lateinit var destination: LatLng
        val cities = resources.getStringArray(R.array.USCities)

        // Autocompletes text user inputs in source and destination fields
        setSearchResults(startLocationInput, cities)
        setSearchResults(destLocationInput, cities)

        // Create a value to access the button to get directions
        val dirButton: Button = findViewById(R.id.dirButton)


        // Find directions when the button is pushed
        dirButton.setOnClickListener {
            Log.d("DEBUG", "Directions clicked")
            // Set destination coordinates
            if (destLocationInput.toString().isEmpty()) {
                // Show pop-up saying that destination is required

            } else {
                getCityCoords(destLocationInput.toString()) { coords ->
                    destination = coords
                }
            }


            // Set start coordinates
            if (startLocationInput.toString().isEmpty()) {
                start = curLocation
            } else {
                getCityCoords(startLocationInput.toString()) { coords ->
                    start = coords
                }
            }

            //Thread.sleep(500)

            // Creating LatLngBounds obj to create a "bounds" for what is displayed on the map
            if (destination != LatLng(0.0, 0.0) && start != LatLng(0.0, 0.0)) {
                // Set the bounds of the route to the start and the destination
                val routeBounds = LatLngBounds.builder()
                routeBounds.include(start).include(destination)

                mMap.clear()
                // Add markers to the start point and destination
                mMap.addMarker(MarkerOptions().position(start).title("Origin"))
                mMap.addMarker(MarkerOptions().position(destination).title("Destination"))

                // Move the camera to the optimal point to show both bounds of the route
                mMap.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        routeBounds.build(),
                        1000,
                        1000,
                        0
                    )
                )

                // Calculate the optimal route for the user's requested directions
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

    // Performs operations related to the map once it is ready
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(googleMap: GoogleMap) {
        // Set the global mMap variable to point to the now initialized googleMap
        mMap = googleMap

        // Request permissions and enable the button to recenter the map
        enableLocationButton()

        // Prepare an intent to interface with notifications
        //val notificationIntent = Intent(this, NotificationActivity::class.java)

        // Prepare a variable to represent the marker for the user's location and
        // prepare another variable to make sure that it does not get created more than once.
        lateinit var userMarker: Marker
        var markerCreated = false

        // Use a thread so that other processes do not have to wait for the location or weather
        Thread {
            // Use an infinite loop to run as long as the app is active
            while (true) {
                Log.d("DEBUG", "Entered thread")

                // Get the user's current location and then wait to let the location return
                curLocation = LocationClass.calling(this)
                Log.d("DEBUG", "Location: $curLocation")
                Thread.sleep(500)

                // Make sure that the location has been updated to avoid errors in future sections
                if(curLocation.latitude != 0.0) {
                    Log.d("DEBUG", "Inside weather")
                    // Get the weather using the shortForecast and continue to the rest of the operations
                    WeatherClass.getWeatherData(curLocation, 0, "shortForecast") { weather ->

                        Log.d("weatherresult", weather)



                        // Find the weather icon corresponding to the user's current location to use
                        // as the image for the user marker

                        val Icon = WeatherParser(weather, this).img
                        val userIcon = Bitmap.createScaledBitmap(Icon, 150, 150, false)

                        // Display a notification for testing purposes
                        //NotificationClass.sendNotification(this, weather, weather, userIcon)

                        // Run necessary processes on the ui thread
                        runOnUiThread {
                            // If the map is currently set to follow the user, keep the camera moving
                            // with them.
                            if (changeViewToCurLocation) {
                                Log.d("DEBUG", "Camera update")
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation))
                                mMap.moveCamera(CameraUpdateFactory.zoomTo(10f))
                                changeViewToCurLocation = false
                            }



                            // Check if the user's marker has already been created
                            if(!markerCreated) {
                                // If not, create a marker using the already created icon and place
                                // it at the user's location.
                                userMarker = mMap.addMarker(
                                    MarkerOptions().position(curLocation).icon(
                                        BitmapDescriptorFactory.fromBitmap(userIcon)
                                    ).anchor(0.5f,0.5f)
                                )!!

                                // Flip the boolean so that redundant markers will not be created
                                markerCreated = true
                                // Other wise, check if the user has moved and update the marker's
                                // position (and icon if the weather has changed) if so
                            }else if(userMarker.position != curLocation){
                                userMarker.position= curLocation
                                userMarker.setIcon(BitmapDescriptorFactory.fromBitmap(userIcon))

                            }
                        }
                    }
                }
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun setSearchResults(editTextCity: AutoCompleteTextView, cities : Array<String>) {
        // Used to update list of autocomplete cities
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1,
            cities)

        editTextCity.setAdapter(adapter)
        editTextCity.threshold = 1

        // Updates list of autocomplete cities displayed to user
        /*
        editTextCity.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isStartAddress && startCitySelected) {
                    startCitySelected = false
                } else if (!isStartAddress && destCitySelected) {
                    destCitySelected = false
                }

                /*
                Log.d("test01", "in textChangedListener")
                fetchCity(s.toString()) { addresses ->
                    // Store addresses location info
                    addressesInfo = addresses
                    Log.d("test01", "addressesInfo: $addressesInfo")

                    val cities = mutableListOf<String>()

                    // Get addresses in the format "City, State" and put into cities
                    for (address in addresses) {
                        Log.d("test01", "address: $address")
                        val city = "${address.locality}, ${address.adminArea}"
                        cities.add(city)
                    }

                    adapter.clear()

                    // Updating autocomplete search results
                    adapter.addAll(cities)
                    Log.d("test01", "cities added: $cities")
                    adapter.notifyDataSetChanged()
                    
                }
                
                 */
            }
        })

        /*
        // Sets the value of the start or destination if an item in the autocomplete list is selected
        editTextCity.setOnItemClickListener { parent, _, position, _ ->
            val location = parent.getItemAtPosition(position).toString()
            Log.d("test01", "Selected city: $location")

            val cityState = location.split(",")

            // Get latitude and longitude from selected address
            for (addressInfo in addressesInfo) {
                // Looks for matching city and state
                if (addressInfo.locality.equals(cityState[0]) &&
                    addressInfo.adminArea.equals(cityState[1])) {
                    // Store address info with location coordinates in listener
                    if (isStartAddress) {
                        startCitySelected = true
                        startAddress = addressInfo
                        Log.d("test01", "startAddress: $startAddress")
                    } else {
                        destCitySelected = true
                        destAddress = addressInfo
                        Log.d("test01", "destAddress: $destAddress")
                    }
                }
            }
            hideKeyboard()
        }

         */

        // Used to allow user to press "Enter" key to exit keyboard
        // Note: doesn't seem to work
        editTextCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // User pressed the "Enter" key, hide the keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(editTextCity.windowToken, 0)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

         */
    }

    // Gets list of suggested cities to be displayed to the user
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun fetchCity(query: String, listener: (Address?) -> Unit) {
        Log.d("test01", "in fetchCity")
        val geocoder = Geocoder(this)

        geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                Log.d("test01", "OnGeocode called")

                var address : Address? = null

                if (addresses.isNotEmpty()) {
                    address = addresses[0]
                }
                listener(address)

            }

            override fun onError(p0: String?) {
                Log.e("Geocode error", "$p0")
            }
        })
    }

    // Returns the first city that most closely matches the user's input
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getCityCoords (input : String, listener: (LatLng) -> Unit) {
        fetchCity(input) { address ->
            if (address != null && address.countryCode.equals("US") && address.locality != null &&
                address.hasLatitude() && address.hasLongitude()) {
                Log.d("test01", "passing address to listener: $address")
                listener(LatLng(address.latitude, address.longitude))
            } else {
                listener(LatLng(0.0, 0.0))
            }
        }

    }


    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    // Function to take the input addresses and convert them to coordinates
    private fun locNameToLatLng(loc : String) : LatLng {
        // Access the geocoder
        val geocoder = Geocoder(this)

        // Use the geocoder to get an address list from the address name
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
        // Make sure that the address list was valid and not empty
        if (addressList != null) {
            if (addressList.isNotEmpty()) {
                Log.d("DEBUG", "Address list: $addressList")
                // If it worked, return the coordinates of the address
                return LatLng(addressList[0].latitude, addressList[0].longitude)
            } else {
                // Handle case where no results were found
            }
        }

        // Otherwise, return the origin as a failsafe
        return LatLng(0.0, 0.0)
    }

    // Get an image corresponding to the input weather string
    fun getWeatherImage(curWeather: String): Bitmap {
        // Initialize a bitmap variable to return
        lateinit var bitmap: Bitmap

        Log.d("DEBUG", "In displayWeather: $curWeather")
        // Create a dictionary mapping keys relating to weather to the corresponding images
        val badWeather = mapOf(
            "cloudy" to R.drawable.cloudy, "sunny" to R.drawable.sunny,
            "rain" to R.drawable.rain, "clear" to R.drawable.sunny
        )

        // Initialize a variable to make sure that a corresponding image was found
        var badWeatherExists = false

        // Check if forecast is in list of weather types
        for (weatherType in badWeather.keys) {
            // See if the weather type being checked matches the current weather
            if (curWeather.contains(weatherType, ignoreCase = true)) {
                badWeather[weatherType]?.let {
                    // Flip the bad weather variable so that the default bitmap will not be returned
                    badWeatherExists = true

                    // Assign the bitmap to image corresponding to the current weather
                    bitmap = BitmapFactory.decodeResource(resources, it)
                }
                break
            }
        }

        // If no corresponding weather images were found, use a default image
        if (!badWeatherExists) {
            // icon default is lightning for testing
            bitmap = BitmapFactory.decodeResource(resources, R.drawable.lightning)
        }

        // Return the set bitmap
        return bitmap
    }

    // Function to get permission to let google maps access the user's location and enable
    // the recenter button
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun enableLocationButton(){
        // Make sure that the app has permission to access the user's permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            // If not, get them
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1)
        }else{
            // Otherwise, enable the location features
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
        }
    }

    // Function that is called when user grants app permission
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