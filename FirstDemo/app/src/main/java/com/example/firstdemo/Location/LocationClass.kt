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
import com.google.android.gms.maps.model.LatLng

object LocationClass {
    private lateinit var locationCallback : LocationCallback
    private lateinit var locationRequest : LocationRequest
    private var longitude = 0.0
    private var latitude = 0.0
    //private lateinit var context : Activity


    @RequiresApi(Build.VERSION_CODES.M)
    public fun calling(that: Activity): LatLng {

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

        var t = Thread{ getLocation(that)}
        t.start()
        t.join()

        //return latitude to longitude
        return LatLng(latitude, longitude)
    }

    @SuppressLint("MissingPermission")
    private fun getLocation(context: Activity){
        //val act : TextView = findViewById(R.id.)
        val client = LocationServices.getFusedLocationProviderClient(context)

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(context, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), 1)
        }else{
            client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            client.lastLocation.addOnSuccessListener {
                if(it != null) {
                    latitude = it.latitude
                    longitude = it.longitude
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