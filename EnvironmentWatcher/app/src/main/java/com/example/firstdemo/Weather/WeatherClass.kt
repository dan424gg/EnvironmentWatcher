package com.example.firstdemo.Weather

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.concurrent.thread

object WeatherClass {

    private val okHttpClient = OkHttpClient()

    /* Get the short forecast for a specific hour into the future
     *      callback: gives function ability to have a callback functionality
     *      location: LatLng object
     *      hour: how many hours into the day to get weather (DEFAULT = 0)
     *      property: which specific property from period of day (DEFAULT = detailedForecast)
     *
     * Method calls 'getNWSPropertyJSON' which is another callback function.
     */
    fun getWeatherData(location: LatLng, hour: Int = 0, property: String = "detailedForecast", callback: (result: String) -> Unit) {

        getNWSPropertyJSON(location, "forecastHourly") { json ->
            var content = "Unknown"
            try {
                val period =
                    json.getJSONObject("properties").getJSONArray("periods").getString(hour)
                content = JSONObject(period).getString(property)
            } catch(e: JSONException){
                Log.d("DEBUG", "Caught JSON error")
            }

            callback.invoke(content)
        }
    }


    fun getWeatherData(location: LatLng, hour: Int = 0, property: String = "detailedForecast") : String {
        var content = "Insert weather"

        getNWSPropertyJSON(location, "forecastHourly") { json ->
            Log.d("DEBUG", "Inside weather")
//            val content = json.getJSONObject("properties").getJSONObject("elevation").getDouble("value").toString()   // For debugging
            val period = json.getJSONObject("properties").getJSONArray("periods").getString(hour)
            content = JSONObject(period).getString(property)
        }

        return content
    }

    /* Get a specific JSON object from the list of 'properties' given by the initial NWS request
     *      property: specify property wanted
     *
     * Deprecates need for two separate functions to get a specific property
     */
    private fun getNWSPropertyJSON(location: LatLng, property: String, callback: (result: JSONObject) -> Unit) {
        val latitude = location.latitude
        val longitude = location.longitude
        lateinit var json : JSONObject

        thread {
            try {
                json = JSONObject(run("https://api.weather.gov/points/$latitude,$longitude"))
                Log.d("WEATHERSTUFF", "lat: $latitude lng: $longitude")
                json = JSONObject(run(json.getJSONObject("properties").getString(property)))

                callback.invoke(json)
            } catch (e: JSONException) {
                // Catch the error to stop the crashing
                Log.d("DEBUG", "Caught JSON error")

                json = JSONObject()
                callback.invoke(json)
            }
        }
    }

    private fun run(url : String) : String {
        val request = Request.Builder()
            .url(url)
            .header("User-agent", "an agent")
            .build()

        //TODO: handle timeouts (.isSuccessful doesn't handle timeouts, so the app crashes)
        //maybe put this on a loop, in the event of a timeout??
        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    if (response.toString().contains("code=500")) {
                        Log.d("hail", "caught code 500!!")
                        return run(url)
                    } else {
                        throw IOException("$response")
                    }
                }
                return response.body!!.string()
            }
        } catch (e: Exception) {
            Log.d("hail", "caught an exception")
            return ""
        }
    }
}