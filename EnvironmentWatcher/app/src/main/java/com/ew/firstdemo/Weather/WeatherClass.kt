package com.ew.firstdemo.Weather

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.ew.firstdemo.MainActivityViewModel.Result
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

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
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getWeatherData(
        location: LatLng,
        hour: Int = 0,
        property: String = "detailedForecast"
    ): Result<String> {

        return withContext(Dispatchers.IO)
        {
            when (val json = getNWSPropertyJSON(location, "forecastHourly")) {
                is Result.Success<JSONObject> -> {
                    try {
                        val period =
                            json.data!!.getJSONObject("properties").getJSONArray("periods").getString(hour)
                        val content = JSONObject(period).getString(property)
                        Result.Success(content)
                    } catch (e: JSONException) {
                        Log.d("hail", "getWeatherData error: $e")
                        Result.Error(e.toString())
                    }
                }

                is Result.Error<JSONObject> -> {
                    Result.Error(json.message!!)
                }
            }

        }
    }

    /* Get a specific JSON object from the list of 'properties' given by the initial NWS request
     *      property: specify property wanted
     *
     * takes away need for two separate functions to get a specific property
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend private fun getNWSPropertyJSON(
        location: LatLng,
        property: String
    ): Result<JSONObject> {
        val latitude = location.latitude
        val longitude = location.longitude
        lateinit var json: JSONObject

        return withContext(Dispatchers.IO) {
            try {
                json = run("https://api.weather.gov/points/$latitude,$longitude")?.let {
                    JSONObject(
                        it
                    )
                }!!

                json = run(json.getJSONObject("properties").getString(property))?.let {
                    JSONObject(
                        it
                    )
                }!!

                Result.Success(json)
            } catch (e: JSONException) {
                Log.d("hail", "getNWSPropertyJSON error: " + e.toString())
                Result.Error(e.toString())
            }
        }
    }

    private fun run(url: String): String? {
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
            Log.d("hail", "run in WeatherClass error: $e")
            return null
        }
    }
}