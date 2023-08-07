package com.ew.firstdemo.Location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
//import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

object CurrentLocation {
    private var output: LatLng? = null

    // Function that checks location permissions and updates the global location variables
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    fun getLocation(context: Activity): LatLng? {

        // Use a client to access the location API
        val client = LocationServices.getFusedLocationProviderClient(context)

        // Make sure the app was granted the needed permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Get permissions if needed
            ActivityCompat.requestPermissions(context, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1)

        } else {
            // Get the most recent location
            client.lastLocation.addOnSuccessListener {
                if (it != null) { output = LatLng(it.latitude, it.longitude) }
            }
        }
    return output
    }
}