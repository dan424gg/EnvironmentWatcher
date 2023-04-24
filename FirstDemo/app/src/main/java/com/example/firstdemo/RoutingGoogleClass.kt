package com.example.firstdemo

import android.app.Activity
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
import kotlin.concurrent.thread
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.round

/* when you zoom in, show more waypoints
    at "normal" zoom, show waypoints for a range of every 100 miles
    when you zoom in, show more precise waypoints

   have UI based around map view
 */


object RoutingGoogleClass {

    private val client = OkHttpClient()
    private val path: MutableList<List<LatLng>> = ArrayList()
    private var totPath: List<LatLng> = ArrayList()
    private lateinit var json : JSONObject

    // Number of segments of route
    private var numSegments : Int = 6

    fun calling(googleMap: GoogleMap, origin: LatLng, destination: LatLng, activity: Activity) {

        // deconstruct LatLng objects to use in URL
        val originLat = origin.latitude
        val originLng = origin.longitude
        val destLat = destination.latitude
        val destLng = destination.longitude

        // construct URL
        val urlDirections =
            "https://maps.googleapis.com/maps/api/directions/json?origin=$originLat,$originLng&destination=$destLat,$destLng&key=AIzaSyCATUJigQ64LYkCQ5NkBYkaiSO8PWwL8ec"

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
        val legs = routes.getJSONObject(0).getJSONArray("legs")
        val duration = legs.getJSONObject(0).getJSONObject("duration").getInt("value")
        val steps = legs.getJSONObject(0).getJSONArray("steps")

        // On each step, decode points on polyline for route segment and add a segmented path to the list
        for (i in 0 until steps.length()) {
            val points = steps.getJSONObject(i).getJSONObject("polyline").getString("points")
            path.add(PolyUtil.decode(points))
        }

        thread {
            displayRoute(googleMap, activity)
        }
        checkCondOfRoute(googleMap, duration, activity)
    }

    private fun displayRoute(googleMap: GoogleMap, activity: Activity) {

        // On each segment of path, update UI map with path
        activity.runOnUiThread {
            for (i in 0 until path.size) {
                googleMap.addPolyline(
                    PolylineOptions().addAll(path[i]).color(
                        android.graphics.Color.BLUE
                    )
                )
            }
        }
    }

    private fun checkCondOfRoute(googleMap: GoogleMap, duration: Int, activity: Activity) {

        // Convert cur time to seconds to get more accurate weather results later
        val curSeconds = curTimeToSeconds()

        // Flatten path list of LatLng objects
        totPath = path.flatten()

        // Find first segment of flattened path list, subtract one to not allow index go out of bounds (so segmentIdx != totPath.size)
        val segmentIdx = floorDiv(totPath.size, numSegments) - 1

        // Split time based on numSplits
        val segmentTime = floorDiv(duration, numSegments)

        for (i in 1 until numSegments) {
            thread {
                // Add cur time to segmentTime
                val futureTime = (segmentTime * i)

                // Round futureTime to nearest hour
                val hour = round((futureTime / 60.0) / 60.0).toInt()

                val location = totPath[segmentIdx * i]
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
