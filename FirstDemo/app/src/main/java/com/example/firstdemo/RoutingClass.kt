package com.example.firstdemo

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import com.example.firstdemo.Weather.WeatherClass
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.PolyUtil
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
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

   have UI based around map view
 */


object RoutingClass {

    private val client = OkHttpClient()
    private var path: List<LatLng> = ArrayList()
    private lateinit var steps: JSONArray
    private var distance: Double = 0.0
    private var duration: Double = 0.0

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
            "https://api.mapbox.com/directions/v5/mapbox/driving/$originLng,$originLat;$destLng,$destLat?geometries=polyline&steps=true&access_token=pk.eyJ1IjoiZGFuNDI0Z2ciLCJhIjoiY2xlZXF2cDBsMDB5NjN6dWwwM2F3YWc1ZCJ9.7pQJfMN_gPpJNBJ2PqqnzQ"

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
        val polyline = routes.getJSONObject(0).getString("geometry")

        duration = routes.getJSONObject(0).getDouble("duration")
        distance = routes.getJSONObject(0).getDouble("distance")
        steps = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps")

        path = PolyUtil.decode(polyline)

        thread {
            displayRoute(googleMap, activity)
        }
        checkCondOfRoute(googleMap, activity)
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

    private fun checkCondOfRoute(googleMap: GoogleMap, activity: Activity) {

        // Find first segment of flattened path list, subtract one to not allow index go out of bounds (so segmentIdx != path.size)
        val segmentIdx = floorDiv(path.size, numSegments) - 1

        // Split time based on numSplits
        val segmentTime = floorDiv(duration.toInt(), numSegments)

        for (i in 1 until numSegments) {
            thread {
                // Add cur time to segmentTime
                val futureTime = (segmentTime * i)

                // Get list of locations to do markers
                val locations = getLocations()

                // Round futureTime to nearest hour
                val hour = round((futureTime / 60.0) / 60.0).toInt()

                val location = path[segmentIdx * i]

                WeatherClass.getWeatherData(location, hour, "shortForecast") { result ->
                    val locIcon =
                        Bitmap.createScaledBitmap((activity as MainActivity).getWeatherImage(result), 150, 150, false)
                    activity.runOnUiThread {
                        googleMap.addMarker(MarkerOptions().position(location).title(result).icon(BitmapDescriptorFactory.fromBitmap(locIcon)))
                    }
                }
            }
        }
    }

    private fun getLocations() : List<LatLng>{

        val output: MutableList<LatLng> = ArrayList()
        val segment = distance / numSegments
        var sum = 0.0

        for (i in 0 until steps.length()) {
            /* can't split based on distance from step format
                    might try going off of duration
                        could potentially go into intersections and
                        somehow go into the list, get each little duration,
                        check everytime and keep track of spot in list.
                    figure out some other way
                 */

            val dist = steps.getJSONObject(i).getDouble("distance")

            sum += dist

            if (sum >= segment) {
                // Get location
                val locations = (steps.getJSONObject(i).getJSONArray("intersections").getJSONObject(0).getJSONArray("location"))

                // "Split" location into a latitude and longitude
                val loc = LatLng(locations.get(1).toString().toDouble(), locations.get(0).toString().toDouble())
                output.add(loc)

//                sum = 0.0
            }
        }

        return output
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