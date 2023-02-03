package com.example.firstdemo

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.firstdemo.Location.LocationActivity
import com.example.firstdemo.Location.LocationViewModel
import com.example.firstdemo.Weather.WeatherActivity
import com.example.firstdemo.Weather.WeatherViewModel
import kotlinx.coroutines.Runnable

class MainActivity : AppCompatActivity() {
    private var latitude = 0.0
    private var longitude = 0.0
    private var weather: String = "com/example/firstdemo/Weather"
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()
    private var hasLocation = false
    private lateinit var weatherIntent: Intent
    private lateinit var locationIntent: Intent
    lateinit var updateData: Runnable
    lateinit var mainHandler: Handler

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lat: TextView = findViewById(R.id.latitude)
        val long: TextView = findViewById(R.id.longitude)
        val currWeather: TextView = findViewById(R.id.weather)
        val locationButton: Button = findViewById(R.id.loc_button)
        val weatherButton: Button = findViewById(R.id.weather_button)
        // Create Live Data obj
        val forecastObserver = Observer<String> { newWeather ->
            Log.d("DEBUG", "Weather changed!")
            currWeather.text = newWeather
        }
        weatherViewModel.forecast.observe(this, forecastObserver)

        val longitudeObserver = Observer<Double> { newLongitude ->
            long.text = newLongitude.toString()
        }
        locationViewModel.liveLongitude.observe(this, longitudeObserver)

        val latitudeObserver = Observer<Double> { newLatitude ->
            lat.text = newLatitude.toString()
        }
        locationViewModel.liveLatitude.observe(this, latitudeObserver)


        val locationResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == Activity.RESULT_OK){
                // Handle result data
                if(it.data != null) {
                    latitude = it.data!!.getDoubleExtra("latitude", latitude)
                    longitude = it.data!!.getDoubleExtra("longitude", longitude)

                    locationViewModel.liveLatitude.setValue(latitude)
                    locationViewModel.liveLongitude.setValue(longitude)
                    hasLocation = true
                }
            }
        }

        val weatherResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == Activity.RESULT_OK){
                // Handle result data
                if(it.data != null) {
                    weather = it.data!!.getStringExtra("weather").toString()
                    weatherViewModel.forecast.setValue(weather)
                }
            }
        }

        locationIntent = Intent(this, LocationActivity::class.java).also{
            it.putExtra("latitude", latitude)
            it.putExtra("longitude", longitude)
        }

        updateData = Runnable {
            locationResult.launch(locationIntent)
            if(hasLocation) {
                weatherIntent = Intent(this, WeatherActivity::class.java).also {
                    it.putExtra("latitude", latitude)
                    it.putExtra("longitude", longitude)
                }

                weatherResult.launch(weatherIntent)
            }
            mainHandler.postDelayed(updateData, 2000)
        }

        mainHandler = Handler(Looper.getMainLooper())
        mainHandler.postDelayed(updateData, 2000)
    }
}