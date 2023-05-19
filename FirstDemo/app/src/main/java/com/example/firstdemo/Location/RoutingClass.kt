package com.example.firstdemo.Location

import android.app.Activity
import android.graphics.Bitmap
import com.example.firstdemo.Weather.WeatherClass.getWeatherData
import com.example.firstdemo.Weather.WeatherParser
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

        // Split time based on numSplits
        val segmentTime = floorDiv(duration, numWaypoints)

        val locations = getLocations(steps)

        for (i in 1 until locations.size) {

            // Add cur time to segmentTime
            val futureTime = (segmentTime * i)

            // Round futureTime to nearest hour
            val hour = round((futureTime / 60.0) / 60.0).toInt()

            getWeatherData(locations[i], hour, "shortForecast") { result ->
                val locIcon =
                    Bitmap.createScaledBitmap(
                        WeatherParser(
                            result,
                            activity.applicationContext
                        ).img, 150, 150, false
                    )

                activity.runOnUiThread {
                    googleMap.addMarker(
                        MarkerOptions().position(locations[i]).title(result).icon(
                            BitmapDescriptorFactory.fromBitmap(locIcon)
                        )
                    )
                }
            }
        }
    }

    private fun getLocations(steps: JSONArray) : List<LatLng> {

        // Master list of coordinates
        val coordsList = mutableListOf<LatLng>()

        // List of distances with it's corresponding 'to' and 'from' coordinates
        val distWCoords = mutableListOf<Pair<Double,Pair<LatLng, LatLng>>>()

        // Output list of waypoint locations
        val output: MutableList<LatLng> = ArrayList()

        // Total distance of route
        var totDistance = 0.0

        // Initialize coordsList with origin coordinates
        coordsList.add(LatLng(originLat, originLng))

        // For each step in a route, find the coordinates at each intersection
        for (i in 1 until steps.length()) {

            // Get the array of intersections and the length of the array
            val intersections = steps.getJSONObject(i).getJSONArray("intersections")
            val intersectionsLength = intersections.length()

            // Add the coordinate of each intersection into coordsList
            for (w in 0 until intersectionsLength) {
                val long = intersections.getJSONObject(w).getJSONArray("location").getDouble(0)
                val lat = intersections.getJSONObject(w).getJSONArray("location").getDouble(1)

                coordsList.add(LatLng(lat, long))

                // At each coordinate, find the distance between the current coordinate and the most
                // recent coordinate, add to totDistance and log values in distWCoords
                val distance = distance(coordsList[coordsList.size - 2], coordsList[coordsList.size - 1])
                totDistance += distance

                distWCoords.add(Pair(distance, Pair(coordsList[coordsList.size - 2], coordsList[coordsList.size - 1])))
            }
        }

        // Find the default segmentSize for route
        val segmentSize = totDistance / numWaypoints

        // Used to keep track of running total of distances (Offset for weird bug with first location
        var sum = ((segmentSize * 4) / 5)

        // At each set of coordinates, sum up the corresponding distance
        for (item in distWCoords) {
            sum += item.first

            if (sum >= segmentSize) {
                sum -= segmentSize

                val x2 = item.second.second.latitude
                val y2 = item.second.second.longitude

                output.add(LatLng(x2, y2))
            }
        }

        return output
    }

    private fun distance(coord1: LatLng, coord2: LatLng): Double {

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