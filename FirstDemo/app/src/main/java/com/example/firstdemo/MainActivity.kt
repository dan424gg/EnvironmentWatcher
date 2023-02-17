package com.example.firstdemo

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdemo.Location.LocationClass
import com.example.firstdemo.Weather.WeatherClass

class MainActivity : AppCompatActivity() {
    private var latitude = 0.0
    private var longitude = 0.0
    private var weather: String = "Weather"
    private var runThread = true

    
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val lat: TextView = findViewById(R.id.latitude)
        val long: TextView = findViewById(R.id.longitude)
        val curWeather: TextView = findViewById(R.id.weather)
        val locationButton: Button = findViewById(R.id.loc_button)
        val weatherButton: Button = findViewById(R.id.weather_button)

        val notificationIntent = Intent(this, NotificationActivity::class.java)

        locationButton.setOnClickListener{
            runThread = !runThread
        }

        Thread(Runnable {
            // Runs only when Button is True
            while (runThread) {
                Log.d("DEBUG","Entered thread")
                val (p_lat, p_long) = LocationClass.calling(this)
                latitude = p_lat
                longitude = p_long
                runOnUiThread {
                    lat.text = latitude.toString()
                    long.text = longitude.toString()
                }
                Thread.sleep(2500)
                Log.d("DEBUG","Entered thread 2")
                if(latitude != 0.0) {
                    val forecast = WeatherClass.calling(latitude, longitude)
                    runOnUiThread {
                        curWeather.text = forecast
                    }
                }
                Thread.sleep(2500)
            }
        }).start()

        /*
        Thread(Runnable {
            // Runs only when Button is True
            while (runThread) {
                Thread.sleep(2500)
                val forecast = WeatherClass.calling(latitude, longitude)
                runOnUiThread {
                    curWeather.text = forecast
                }
                Thread.sleep(2500)
            }
        }).start()
         */

        weatherButton.setOnClickListener{
            runThread = !runThread
        }


    }

}
