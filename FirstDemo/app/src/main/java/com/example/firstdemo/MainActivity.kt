package com.example.firstdemo

import android.Manifest
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
        createNotificationChannel()


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

        // commented code below (and .setContentIntent() below) will allow notification to be interactive)
        /*
        val intent = Intent(this, AlertDetails::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        */

        var builder = NotificationCompat.Builder(this, "0")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("My notification")
            .setContentText("Much longer text that cannot fit one line...")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Much longer text that cannot fit one line..."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        //.setContentIntent(pendingIntent)
            .setAutoCancel(true)

        //notify(notificationId, builder.build())else {
        //                        Log.d("aidan", "notification settings not enabled")
        //                    }

        locationButton.setOnClickListener{
            locationResult.launch(locationIntent)
            lat.text = latitude.toString()
            long.text = longitude.toString()


            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                val notificationId = 0
                notify(notificationId, builder.build())
            }

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

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)

            //Importance value determines if notification should replace last notification from same channel id
            //set priority with setPriority()
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("0", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}