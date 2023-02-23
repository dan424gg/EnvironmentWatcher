package com.example.firstdemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices

class LocationActivity : AppCompatActivity(){
    private lateinit var locationCallback : LocationCallback
    private lateinit var locationRequest : LocationRequest
    private var longitude = 0.0
    private var latitude = 0.0


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_location)
        Log.d("DEBUG", "Reached second activity")

        longitude = intent.getDoubleExtra("longitude", latitude)
        latitude = intent.getDoubleExtra("latitude", longitude)

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

        getLocation()
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLocation(){
        Log.d("DEBUG", "Reached getLocation")
        val client = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
            ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            Log.d("DEBUG", "Reached Ask Permission")
            ActivityCompat.requestPermissions(this, arrayOf(
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
                    val returnIntent = Intent(this, MainActivity::class.java).also{itData->
                        itData.putExtra("latitude", latitude)
                        itData.putExtra("longitude", longitude)
                    }
                    setResult(Activity.RESULT_OK, returnIntent)
                    finish()
                }
            }
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
            getLocation()
        }
    }
}