package com.example.firstdemo.Weather

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import com.example.firstdemo.MainActivity
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

object WeatherClass {

    private val _client = OkHttpClient()
    private var latitude = 10.0
    private var longitude = 10.0
    private var output : String = "Forecast goes here!"

    public fun calling(location: LatLng): String {

        latitude = location.latitude
        longitude = location.longitude
        //var o : Object = Object()

        thread {
            val json = getNWSData()
            output = getWeatherString(json)
            //o.notifyAll()
        }

        //o.wait()
        Thread.sleep(3000)

        return output
    }

    fun getNWSData() : JSONObject {
        var json = JSONObject(run("https://api.weather.gov/points/$latitude,$longitude"))

        // Doing some JSON parsing stuff to get gridpoint URL
        json = JSONObject(run(json.getJSONObject("properties").getString("forecast")))

        return json
    }

    fun getWeatherString(json: JSONObject) : String{
        val content =
            json.getJSONObject("properties").getJSONArray("periods").getString(0)
        val moreContent = JSONObject(content)
        return moreContent.getString("detailedForecast")
//        forecast.setValue(moreContent.getString("detailedForecast"))
    }

    private fun run(url : String) : String {
        val request = Request.Builder()
            .url(url)
            .header("User-agent", "an agent")
            .build()

        _client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("$response")
            return response.body!!.string()
        }
    }

}