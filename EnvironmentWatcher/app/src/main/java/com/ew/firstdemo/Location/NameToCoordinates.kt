package com.ew.firstdemo.Location

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ew.firstdemo.MainActivity
import com.google.android.gms.maps.model.LatLng

object NameToCoordinates {

    // Returns the first city that most closely matches the user's input
    fun getCityCoords(
        start: String,
        destination: String,
        activity: MainActivity,
        listener: (Pair<LatLng?, LatLng>) -> Unit
    ) {
        var startCoords: LatLng?
        var destinationCoords: LatLng

        // Get the coordinates based on the location string
        //
        // We want to have the startCoords callback inside of the destinationCoords callback to have a sequential process
        // and to make sure the function doesn't return before all coordinates have been found
        if (Build.VERSION.SDK_INT >= 33) {
            fetchCityCoords(destination, activity) { destinationAddress ->

                // If destinationAddress is valid, set it as destinationCoords
                // else, set equal to (1.0, 1.0) for debugging
                destinationCoords =
                    if (destinationAddress != null && destinationAddress.countryCode.equals("US") &&
                        destinationAddress.hasLatitude() && destinationAddress.hasLongitude()
                    ) {
                        LatLng(destinationAddress.latitude, destinationAddress.longitude)
                    } else {
                        LatLng(1.0, 1.0)
                    }

                // If nothing is passed to the start input field, set startCoords to the user's current location
                // else, do same as getting destinationCoords
                if (start != "") {
                    fetchCityCoords(start, activity) { startAddress ->
                        startCoords =
                            if (startAddress != null && startAddress.countryCode.equals("US") &&
                                startAddress.hasLatitude() && startAddress.hasLongitude()
                            ) {
                                LatLng(startAddress.latitude, startAddress.longitude)
                            } else {
                                LatLng(1.0, 1.0)
                            }
                        listener.invoke(
                            Pair(
                                startCoords, destinationCoords
                            )
                        )
                    }
                } else {
                    listener.invoke(
                        Pair(
                            LatLng(48.754902, -122.478119), destinationCoords
                        )
                    )
                }
            }
        } else {
            startCoords = locNameToLatLng(start, activity)
            destinationCoords = locNameToLatLng(destination, activity)

            listener.invoke(Pair(startCoords, destinationCoords))
        }
    }

    // Gets list of suggested cities to be displayed to the user
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun fetchCityCoords(
        query: String, activity: MainActivity, listener: (Address?) -> Unit
    ) {
        val geocoder = Geocoder(activity)

        // Get a single set of coordinates based on query string
        geocoder.getFromLocationName(query, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                var address: Address? = null

                if (addresses.isNotEmpty()) {
                    address = addresses[0]
                }

                listener.invoke(address)
            }

            override fun onError(p0: String?) {}
        })
    }

    // Function to take the input addresses and convert them to coordinates
    //
    // ONLY FOR API < TIRAMISU
    private fun locNameToLatLng(loc: String, activity: MainActivity): LatLng {
        // Access the geocoder
        val geocoder = Geocoder(activity)

        // Use the geocoder to get an address list from the address name
        val addressList = geocoder.getFromLocationName(loc, 1)

        // Make sure that the address list was valid and not empty
        if (addressList != null) {
            if (addressList.isNotEmpty()) {
                // If it worked, return the coordinates of the address
                return LatLng(addressList[0].latitude, addressList[0].longitude)
            } else {
                // Handle case where no results were found
            }
        }

        // Otherwise, return the origin as a failsafe
        return LatLng(0.0, 0.0)
    }
}
