package com.example.firstdemo

import android.app.Activity
import android.graphics.Bitmap
import android.util.Log
import com.example.firstdemo.Weather.WeatherClass.getWeatherData
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
    private var totDistance: Double = 0.0
    private var duration: Double = 0.0
    var originLat = 0.0
    var originLng = 0.0
    var destLat = 0.0
    var destLng = 0.0


    // Number of segments in route
    private const val numSegments = 10

    fun calling(googleMap: GoogleMap, origin: LatLng, destination: LatLng, activity: Activity) {

        // deconstruct LatLng objects to use in URL
        originLat = origin.latitude
        originLng = origin.longitude
        destLat = destination.latitude
        destLng = destination.longitude

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
        totDistance = routes.getJSONObject(0).getDouble("distance")
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
//        val segmentIdx = floorDiv(path.size, numSegments + 1) - 1

        // Split time based on numSplits
        val segmentTime = floorDiv(duration.toInt(), numSegments)

        // Get list of locations to do markers
        val locations = getLocations()

        for (i in 1 until locations.size) {
            thread {
                // Add cur time to segmentTime
                val futureTime = (segmentTime * i)

                // Round futureTime to nearest hour
                val hour = round((futureTime / 60.0) / 60.0).toInt()

                getWeatherData(locations[i], hour, "shortForecast") { result ->
                    val locIcon =
                        Bitmap.createScaledBitmap((activity as MainActivity).getWeatherImage(result), 150, 150, false)
                    activity.runOnUiThread {
                        googleMap.addMarker(MarkerOptions().position(locations[i]).title(result).icon(BitmapDescriptorFactory.fromBitmap(locIcon)))
                    }
                }
            }
        }
    }

    private fun getLocations() : List<LatLng>{

        var sum = 0.0
        val output: MutableList<LatLng> = ArrayList()
        val segment = totDistance / (numSegments)
        var x1 = originLat
        var y1 = originLng

        for (i in 0 until steps.length() - 1) {

            val dist = steps.getJSONObject(i).getDouble("distance")

            val multiplier = dist / segment
            Log.d("routingstuff", "$multiplier")
            sum += dist
            if (sum >= segment || multiplier > 1) {
                sum = segment - sum

                var locations =
                    steps.getJSONObject(i + 1).getJSONArray("intersections").getJSONObject(0)
                        .getJSONArray("location")
                val x2 = locations.get(1).toString().toDouble()
                val y2 = locations.get(0).toString().toDouble()

                var count = 1
                while (count < (multiplier + 1)) {
                    val newx = (x1 + ((count / (multiplier + 1)) * (x2 - x1)))
                    val newy = (y1 + ((count / (multiplier + 1)) * (y2 - y1)))
                    output.add(LatLng(newx, newy))
                    count += 1
                }

                locations =
                    steps.getJSONObject(i).getJSONArray("intersections").getJSONObject(0)
                        .getJSONArray("location")
                x1 = locations.get(1).toString().toDouble()
                y1 = locations.get(0).toString().toDouble()
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