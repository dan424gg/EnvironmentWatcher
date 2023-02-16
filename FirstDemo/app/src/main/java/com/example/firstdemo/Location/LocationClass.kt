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
import com.google.android.gms.location.LocationServices

object LocationClass {
    private lateinit var locationCallback : LocationCallback
    private lateinit var locationRequest : LocationRequest
    private var longitude = 0.0
    private var latitude = 0.0
    //private lateinit var context : Activity


    @RequiresApi(Build.VERSION_CODES.M)
    public fun calling(that: Activity): Pair<Double, Double> {
        Log.d("DEBUG", "Reached location code")

        //context = that;

        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 20000
        //getLocation()

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations){
                    longitude = location.longitude
                    latitude = location.latitude
                }
            }
        }
        Log.d("DEBUG", "Reached second activity 2")

        getLocation(that)
        //return latitude to longitude
        return latitude to longitude
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLocation(context: Activity){
        Log.d("DEBUG", "Reached getLocation")
        //val act : TextView = findViewById(R.id.)
        val client = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            Log.d("DEBUG", "Reached Ask Permission")
            ActivityCompat.requestPermissions(context, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1)
        }else{
            client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            client.lastLocation.addOnSuccessListener {
                if(it != null) {
                    Log.d("DEBUG", "Reached Update location")
                    latitude = it.latitude
                    longitude = it.longitude
                    Log.d("DEBUG", latitude.toString())
                }
            }
        }
    }

    /*
    @RequiresApi(Build.VERSION_CODES.M)
    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1){
            // Got permission from user
            getLocation()
        }
    }*/
}