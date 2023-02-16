package com.example.firstdemo.Weather

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class WeatherViewModel : ViewModel(){
    private val _client = OkHttpClient()
    private var _latitude = 10.0
    private var _longitude = 10.0

    val forecast: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    var latitude: Double
        get() = _latitude
        set(value: Double) {_latitude = value}

    var longitude: Double
        get() = _longitude
        set(value: Double) {_longitude = value}

    fun getNWSData() : JSONObject {
        var json = JSONObject(run("https://api.weather.gov/points/$_latitude,$_longitude"))

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

    fun run(url : String) : String {
        val request = Request.Builder()
            .url(url)
            .header("User-agent", "an agent")
            .build()

        _client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("$response")
            Log.d("HailHydra", "Got URL successfully")
            return response.body!!.string()
        }
    }
}