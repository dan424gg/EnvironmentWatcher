package com.example.firstdemo

import android.app.Activity
import android.util.Log
import com.example.firstdemo.Weather.WeatherClass
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.PolyUtil
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.lang.Math.floorDiv
import java.lang.System.exit
import kotlin.concurrent.thread
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.round

/* when you zoom in, show more waypoints
    at "normal" zoom, show waypoints for a range of every 100 miles
    when you zoom in, show more precise waypoints
        zoom goes to 22

   have UI based around map view
 */


object RoutingClass {

    private val client = OkHttpClient()
    private var path: List<LatLng> = ArrayList()
    private lateinit var json : JSONObject

    // Number of segments in route
    private var numSegments : Int = 6

    fun calling(googleMap: GoogleMap, origin: LatLng, destination: LatLng, activity: Activity) {

        // deconstruct LatLng objects to use in URL
        val originLat = origin.latitude
        val originLng = origin.longitude
        val destLat = destination.latitude
        val destLng = destination.longitude

        // construct URL
        val urlDirections =
            "https://api.mapbox.com/directions/v5/mapbox/driving/$originLng,$originLat;$destLng,$destLat?geometries=polyline&access_token=pk.eyJ1IjoiZGFuNDI0Z2ciLCJhIjoiY2xlZXF2cDBsMDB5NjN6dWwwM2F3YWc1ZCJ9.7pQJfMN_gPpJNBJ2PqqnzQ"

        thread {
            getJSONObject(urlDirections) { json ->
                getRoute(json, googleMap, activity)
            }
        }
    }

    // Get routes
    private fun getRoute(json: JSONObject, googleMap: GoogleMap, activity: Activity) {

        // Parse JSON object into noteworthy objects
        val routes = json.getJSONArray("routes")
        val duration = routes.getJSONObject(0).getInt("duration")
        val polyline = routes.getJSONObject(0).getString("geometry")

        path = PolyUtil.decode(polyline)

        thread {
            displayRoute(googleMap, activity)
        }
        checkCondOfRoute(googleMap, duration, activity)
    }

    private fun displayRoute(googleMap: GoogleMap, activity: Activity) {

        // On each segment of path, update UI map with path
        activity.runOnUiThread {
            googleMap.addPolyline(
                PolylineOptions().addAll(path).color(
                    android.graphics.Color.BLUE
                )
            )
        }
    }

    private fun checkCondOfRoute(googleMap: GoogleMap, duration: Int, activity: Activity) {

        // Find first segment of flattened path list, subtract one to not allow index go out of bounds (so segmentIdx != path.size)
        val segmentIdx = floorDiv(path.size, numSegments) - 1

        // Split time based on numSplits
        val segmentTime = floorDiv(duration, numSegments)

        for (i in 1 until numSegments) {
            thread {
                // Add cur time to segmentTime
                val futureTime = (segmentTime * i)

                // Round futureTime to nearest hour
                val hour = round((futureTime / 60.0) / 60.0).toInt()

                val location = path[segmentIdx * i]
                WeatherClass.getWeatherData(location, hour, "shortForecast") { result ->

                    activity.runOnUiThread {
                        googleMap.addMarker(MarkerOptions().position(location).title(result))
                    }
                }
            }
        }
    }

    private fun getJSONObject(url : String, callback: (result: JSONObject) -> Unit) {

        val string : String
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("$response")
            string = response.body!!.string()
        }

        callback.invoke(JSONObject(string))
    }

    // Convert current time to seconds
    private fun curTimeToSeconds() : Double {

        val curTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).split(":")
        val minutes = curTime[0].toFloat() * 60.0
        val curSeconds = (minutes * 60.0) + (curTime[1].toFloat() * 60.0) + curTime[2].toFloat()

        return curSeconds
    }
}