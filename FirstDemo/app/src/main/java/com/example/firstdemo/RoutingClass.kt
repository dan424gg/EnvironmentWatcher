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
//import java.lang.Math.sqrt
import kotlin.concurrent.thread
import kotlin.math.*

/* when you zoom in, show more waypoints
    at "normal" zoom, show waypoints for a range of every 100 miles
    when you zoom in, show more precise waypoints
        zoom goes to 22

   have UI based around map view
 */
object RoutingClass {

    private val client = OkHttpClient()
    private var path: List<LatLng> = ArrayList()
    private var originLat: Double = 0.0
    private var originLng: Double = 0.0
    private var destLat: Double = 0.0
    private var destLng: Double = 0.0

    // Number of segments in route
    private var numWaypoints : Int = 5

    fun calling(googleMap: GoogleMap, origin: LatLng, destination: LatLng, activity: Activity) {

        // deconstruct LatLng objects to use in URL
        originLat = origin.latitude
        originLng = origin.longitude
        destLat = destination.latitude
        destLng = destination.longitude

        // construct URL
        val urlDirections =
            "https://api.mapbox.com/directions/v5/mapbox/driving/$originLng,$originLat;$destLng,$destLat?steps=true&geometries=polyline&access_token=pk.eyJ1IjoiZGFuNDI0Z2ciLCJhIjoiY2xlZXF2cDBsMDB5NjN6dWwwM2F3YWc1ZCJ9.7pQJfMN_gPpJNBJ2PqqnzQ"

        thread {
            getJSONObject(urlDirections) { json ->

                getRouteInfo(json, googleMap, activity)
                displayRoute(googleMap, activity)
            }
        }
    }

    // Get routes
    private fun getRouteInfo(json: JSONObject, googleMap: GoogleMap, activity: Activity) {

        // Parse JSON object into noteworthy objects
        val routes = json.getJSONArray("routes")
        val duration = routes.getJSONObject(0).getInt("duration")
        val polyline = routes.getJSONObject(0).getString("geometry")

        val steps = routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps")
//        val totDistance = routes.getJSONObject(0).getDouble("distance")

        path = PolyUtil.decode(polyline)
        checkCondOfRoute(googleMap, duration, activity, steps)
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

    private fun checkCondOfRoute(googleMap: GoogleMap, duration: Int, activity: Activity, steps: JSONArray) {

        val numSegments = numWaypoints + 1;
        // Find first segment of flattened path list, subtract one to not allow index go out of bounds (so segmentIdx != path.size)
        val segmentIdx = floorDiv(path.size, numSegments)

        // Split time based on numSplits
        val segmentTime = floorDiv(duration, numSegments)

        val locations = getLocations(steps)

        for (i in 1 until locations.size) {

            // Add cur time to segmentTime
            val futureTime = (segmentTime * i)

            // Round futureTime to nearest hour
            val hour = round((futureTime / 60.0) / 60.0).toInt()

            getWeatherData(locations[i], hour, "shortForecast") { result ->
                val locIcon =
                    Bitmap.createScaledBitmap((activity as MainActivity).getWeatherImage(result), 150, 150, false)
                activity.runOnUiThread {
                    googleMap.addMarker(MarkerOptions().position(locations[i]).title(result).icon(
                        BitmapDescriptorFactory.fromBitmap(locIcon)))
                }
            }
        }
    }

    private fun getLocations(steps: JSONArray) : List<LatLng> {

        var multiplier = 1.0

        val coordsSet = mutableListOf<LatLng>()
        val distWCoords = mutableListOf<Pair<Double,Pair<LatLng, LatLng>>>()        // Distance paired with 'from' coords and 'to' coords
        val output: MutableList<LatLng> = ArrayList()

        var x1 = originLat
        var y1 = originLng


        // Go through all steps of the route given from the directions
        for (i in 0 until steps.length() - 1) {

            val intersections = steps.getJSONObject(i).getJSONArray("intersections")
            val intersectionsLength = intersections.length()

            for (w in 0..intersectionsLength - 1) {
                val long = intersections.getJSONObject(w).getJSONArray("location").getDouble(0)
                val lat = intersections.getJSONObject(w).getJSONArray("location").getDouble(1)

                coordsSet.add(LatLng(lat, long))
            }
        }

        var totDistance = 0.0
        for (i in 1..coordsSet.count()-1) {
            val distance = distance(coordsSet[i-1], coordsSet[i])
            totDistance += distance

            distWCoords.add(Pair(distance, Pair(coordsSet[i-1], coordsSet[i])))
        }

        val segmentSize = totDistance / (numWaypoints + 1)
        var sum = segmentSize / 2

        for (item in distWCoords) {

            val dist = item.first
            sum += dist

            if (sum >= segmentSize || (dist / segmentSize) > 1) {
                if (dist / segmentSize > 1) {
                    multiplier = dist / segmentSize
                    sum -= segmentSize
                } else {
                    sum -= segmentSize
                }

                val x2 = item.second.second.latitude
                val y2 = item.second.second.longitude

                var count = 1
                while (count <= multiplier) {
                    val newx = (x1 + ((count / (multiplier + 1)) * (x2 - x1)))
                    val newy = (y1 + ((count / (multiplier + 1)) * (y2 - y1)))
                    output.add(LatLng(newx, newy))

                    count += 1;
                }

                x1 = x2
                y1 = y2
                multiplier = 1.0
            }
        }

        return output
    }

    fun distance(coord1: LatLng, coord2: LatLng): Double {

        return sqrt((coord2.latitude - coord1.latitude).pow(2) + (coord2.longitude - coord1.longitude).pow(2))
    }

    fun getJSONObject(url : String, callback: (result: JSONObject) -> Unit) {

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
}