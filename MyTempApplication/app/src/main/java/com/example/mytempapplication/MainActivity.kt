package com.example.mytempapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private var latitude = 48.7698
    private var longitude = -122.4859
    lateinit var forecastInformation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        forecastInformation = findViewById<TextView>(R.id.forecast_information)

        thread {
            // Get content from initial API request with latitude and longitude
            var json = JSONObject(run("https://api.weather.gov/points/$latitude,$longitude"))

            // Doing some JSON parsing stuff to get gridpoint URL
            json = JSONObject(run(json.getJSONObject("properties").getString("forecast")))

            runOnUiThread {
                // Get forecast at index=0 of forecast array
                val content =
                    json.getJSONObject("properties").getJSONArray("periods").getString(0)
                val moreContent = JSONObject(content)
                forecastInformation.text = moreContent.getString("detailedForecast")
            }
        }
    }

    private fun run(url : String) : String {
        val request = Request.Builder()
            .url(url)
            .header("User-agent", "an agent")
            .build()

        client.newCall(request).execute().use { response ->
            Log.d("HailHydra", "Got URL successfully")
            if (!response.isSuccessful) throw IOException("$response")
            return response.body!!.string()
        }
    }
}