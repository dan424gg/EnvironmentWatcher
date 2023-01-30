package com.example.firstdemo.Weather

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.example.firstdemo.MainActivity
import kotlin.concurrent.thread

class WeatherActivity : AppCompatActivity() {

    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.longitude = intent.getDoubleExtra("longitude", viewModel.longitude)
        viewModel.latitude = intent.getDoubleExtra("latitude", viewModel.latitude)


        thread {
            var json = viewModel.getNWSData()
            runOnUiThread {
                val returnIntent = Intent(this, MainActivity::class.java).also{ itData->
                    itData.putExtra("weather", viewModel.getWeatherString(json))
                }
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        }
    }
}