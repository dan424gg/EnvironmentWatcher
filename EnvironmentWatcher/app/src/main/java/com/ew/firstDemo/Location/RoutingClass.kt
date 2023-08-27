package com.ew.firstDemo.Location

import android.os.Build
import android.util.Log
import com.ew.firstDemo.MainActivityViewModel.Result
import androidx.annotation.RequiresApi
import com.ew.firstDemo.Weather.WeatherClass.getWeatherData
import com.google.maps.android.PolyUtil
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.lang.Math.floorDiv
import kotlin.math.*

object RoutingClass {

    private val client = OkHttpClient()
    private var routeData = RouteData()

    // Number of segments in route
    private var numWaypoints: Int = 5

    // Get routes
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getRouteInfo(origin: LatLng, destination: LatLng): RouteData? {

        val urlDirections =
            "https://api.mapbox.com/directions/v5/mapbox/driving/${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}?steps=true&geometries=polyline&access_token=pk.eyJ1IjoiZGFuNDI0Z2ciLCJhIjoiY2xlZXF2cDBsMDB5NjN6dWwwM2F3YWc1ZCJ9.7pQJfMN_gPpJNBJ2PqqnzQ"

        when (val result = getJSONObject(urlDirections)) {
            is Result.Success<JSONObject> -> {
                // Parse JSON object into noteworthy objects
                val routes = result.data!!.getJSONArray("routes")
                val duration = routes.getJSONObject(0).getInt("duration")
                val polyline = routes.getJSONObject(0).getString("geometry")
                val steps =
                    routes.getJSONObject(0).getJSONArray("legs").getJSONObject(0)
                        .getJSONArray("steps")
                routeData.path = PolyUtil.decode(polyline)
                routeData.routeInfo = checkCondOfRoute(duration, steps)
                return routeData
            }

            is Result.Error<JSONObject> -> {
                return null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private suspend fun checkCondOfRoute(duration: Int, steps: JSONArray): List<LocationData>? {
        val tempRouteInfo: MutableList<LocationData> = emptyList<LocationData>().toMutableList()

        // Split time based on numSplits
        val segmentTime = floorDiv(duration, numWaypoints)
        val locations = getLocations(steps)

        for (i in locations.indices) {
            // Add cur time to segmentTime
            val futureTime = (segmentTime * i)

            // Round futureTime to nearest hour
            val hour = round((futureTime / 60.0) / 60.0).toInt()

            when (val weather = getWeatherData(locations[i], hour, "shortForecast")) {
                is Result.Success<String> -> {
                    tempRouteInfo.add(LocationData(locations[i], weather.data))
                }

                is Result.Error<String> -> {
                    Log.d("hail", "inside checkCondOfRoute" + weather.message)
                    return null
                }
            }
        }

        return tempRouteInfo
    }

    private fun getLocations(steps: JSONArray): List<LatLng> {

        // Master list of coordinates
        val coordsList = mutableListOf<LatLng>()

        // List of distances with it's corresponding 'to' and 'from' coordinates
        val distWCoords = mutableListOf<Pair<Double, Pair<LatLng, LatLng>>>()

        // Output list of waypoint locations
        val output: MutableList<LatLng> = ArrayList()

        // Total distance of route
        var totDistance = 0.0

        // Initialize coordsList with origin coordinates
        coordsList.add(
            LatLng(
                steps.getJSONObject(0).getJSONArray("intersections").getJSONObject(0)
                    .getJSONArray("location").getDouble(1),
                steps.getJSONObject(0).getJSONArray("intersections").getJSONObject(0)
                    .getJSONArray("location").getDouble(0)
            )
        )

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
                val distance =
                    distance(coordsList[coordsList.size - 2], coordsList[coordsList.size - 1])
                totDistance += distance

                distWCoords.add(
                    Pair(
                        distance,
                        Pair(coordsList[coordsList.size - 2], coordsList[coordsList.size - 1])
                    )
                )
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

        return sqrt(
            (coord2.latitude - coord1.latitude).pow(2) + (coord2.longitude - coord1.longitude).pow(
                2
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getJSONObject(url: String): Result<JSONObject> {
        return withContext(Dispatchers.IO) {
            try {
                val string: String
                val request = Request.Builder()
                    .url(url)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw java.io.IOException("$response")
                    string = response.body!!.string()
                }
                Result.Success(JSONObject(string))
            } catch (e: Exception) {
                Result.Error(e.toString())
            }
        }
    }
}