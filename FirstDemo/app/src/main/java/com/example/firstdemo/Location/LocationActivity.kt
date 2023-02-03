package com.example.firstdemo.Location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.firstdemo.MainActivity
import com.google.android.gms.location.LocationServices

class LocationActivity : AppCompatActivity() {
    private val viewModel: LocationViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.longitude = intent.getDoubleExtra("longitude", viewModel.longitude)
        viewModel.latitude = intent.getDoubleExtra("latitude", viewModel.latitude)

        viewModel.locationSetUp()
        getLocation()
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun getLocation(){
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
            client.requestLocationUpdates(viewModel.locationRequest, viewModel.locationCallback, Looper.getMainLooper())
            client.lastLocation.addOnSuccessListener {
                if(it != null) {
                    viewModel.latitude = it.latitude
                    viewModel.longitude = it.longitude
                    val returnIntent = Intent(this, MainActivity::class.java).also{ itData->
                        itData.putExtra("latitude", viewModel.latitude)
                        itData.putExtra("longitude", viewModel.longitude)
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