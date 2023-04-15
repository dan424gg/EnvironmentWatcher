package com.example.firstdemo.Location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
//import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng

object LocationClass {
    private var longitude = 0.0
    private var latitude = 0.0


    // Function called by external classes to access the location
    @RequiresApi(Build.VERSION_CODES.M)
    public fun calling(that: Activity): LatLng {
        Log.d("DEBUG", "Reached location code")

        // Calls the main location function to update the global variables
        // and then sleeps to keep it from returning to the main activity too soon
        getLocation(that)
        Thread.sleep(500)

        // Returns location as a LatLng
        return LatLng(latitude, longitude)
    }

    // Function that checks location permissions and updates the global location variables
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLocation(context: Activity){
        Log.d("DEBUG", "Reached getLocation")

        // Use a client to access the location API
        val client = LocationServices.getFusedLocationProviderClient(context)

        // Make sure the app was granted the needed permissions
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            Log.d("DEBUG", "Reached Ask Permission")

            // Get permissions if needed
            ActivityCompat.requestPermissions(context, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1)
        }else{
            // Get the most recent location
            client.lastLocation.addOnSuccessListener {
                if(it != null) {
                    Log.d("DEBUG", "Reached Update location")

                    // If the location was obtained properly (no errors), update the location
                    latitude = it.latitude
                    longitude = it.longitude
                    Log.d("DEBUG", latitude.toString())
                }
            }
        }
    }
}