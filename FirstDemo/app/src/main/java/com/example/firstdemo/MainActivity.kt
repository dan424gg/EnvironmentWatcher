package com.example.firstdemo

import  android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import okhttp3.internal.notify

class MainActivity : AppCompatActivity() {
    private var latitude = 0.0
    private var longitude = 0.0
    private var weather: String = "Weather"

    
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                     Log.d("aidan", "notification permission granted")
                } else {
                    Log.d("aidan", "notification permission denied")
                }
            }
         */


        setContentView(R.layout.activity_main)


        val lat: TextView = findViewById(R.id.latitude)
        val long: TextView = findViewById(R.id.longitude)
        val curr_weather: TextView = findViewById(R.id.weather)
        val locationButton: Button = findViewById(R.id.loc_button)
        val weatherButton: Button = findViewById(R.id.weather_button)

        val locationResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == Activity.RESULT_OK){
                // Handle result data
                if(it.data != null) {
                    Log.d("DEBUG", "BACK TO MAIN")
                    Log.d("DEBUG", it.data!!.getDoubleExtra("latitude", -1.0).toString())
                    latitude = it.data!!.getDoubleExtra("latitude", latitude)
                    longitude = it.data!!.getDoubleExtra("longitude", longitude)
                }
            }
        }

        val weatherResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == Activity.RESULT_OK){
                // Handle result data
                if(it.data != null) {
                    Log.d("DEBUG", "BACK TO MAIN for weather")
                    weather = it.data!!.getStringExtra("weather").toString()
                }
            }
        }

        val locationIntent = Intent(this, LocationActivity::class.java).also{
            it.putExtra("latitude", latitude)
            it.putExtra("longitude", longitude)
        }

        val notificationIntent = Intent(this, NotificationActivity::class.java)

        locationButton.setOnClickListener{
            val (lat2, lon2) = LocationClass.calling(this)
            //startActivity(notificationIntent)

            lat.text = lat2.toString()
            long.text = lon2.toString()


        }

        weatherButton.setOnClickListener{
            val weatherIntent = Intent(this, WeatherActivity::class.java).also{
                it.putExtra("latitude", latitude)
                it.putExtra("longitude", longitude)
            }

            weatherResult.launch(weatherIntent)
            curr_weather.text = weather
        }


    }

}