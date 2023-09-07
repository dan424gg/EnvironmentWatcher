package com.ew.firstdemo.Location

import android.util.Log
import com.ew.firstdemo.MainActivityViewModel.Result
import com.ew.firstdemo.Weather.WeatherClass.getWeatherData
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Math.floorDiv
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.*

object RoutingClass {

    private val client = OkHttpClient()
    private var routeData = RouteData()

    // Number of segments in route
    private var numWaypoints: Int = 5

    // Get routes
    suspend fun getRouteInfo(origin: LatLng, destination: LatLng): RouteData? {
        return withContext(Dispatchers.IO) {
            val urlDirections =
                "https://api.mapbox.com/directions/v5/mapbox/driving/${origin.longitude},${origin.latitude};${destination.longitude},${destination.latitude}?steps=true&geometries=polyline&access_token=pk.eyJ1IjoiZGFuNDI0Z2ciLCJhIjoiY2xlZXF2cDBsMDB5NjN6dWwwM2F3YWc1ZCJ9.7pQJfMN_gPpJNBJ2PqqnzQ"

            when (val result = getJSONObject(urlDirections)) {
                is Result.Success<JSONObject> -> {
                    // Parse JSON object into noteworthy objects
                    val routes = result.data!!.getJSONArray("routes")
                    val duration = routes.getJSONObject(0).getInt("duration")
                    val distance = routes.getJSONObject(0).getInt("distance")
                    val polyline = routes.getJSONObject(0).getString("geometry")

                    routeData.path = PolyUtil.decode(polyline)
                    routeData.routeInfo =
                        checkCondOfRoute(duration, distance, routeData.path!!)
                    routeData
                }

                is Result.Error<JSONObject> -> {
                    null
                }
            }
        }
    }

    private suspend fun checkCondOfRoute(
        duration: Int,
        distance: Int,
        path: List<LatLng>
    ): List<LocationData>? {
        val tempRouteInfo: MutableList<LocationData> = emptyList<LocationData>().toMutableList()

        val segmentDistance = floorDiv(distance, numWaypoints)
        var currentDistance = 0

        val segmentTime = floorDiv(duration, numWaypoints)
        var currentTime = 0

        for (i in path.indices) {
            if (i != 0) {
                currentDistance += distance(path[i], path[i - 1])
            } else {
                // allow the starting point to be displayed on map w/ weather
                currentDistance = segmentDistance
                currentTime = (-segmentTime)
            }

            if (currentDistance >= segmentDistance || i == (path.size - 1)) {
                currentDistance -= segmentDistance
                currentTime += segmentTime

                // Round futureTime to nearest hour
                val hour = round((currentTime / 60.0) / 60.0).toInt()
                when (val weather = getWeatherData(path[i], hour, "shortForecast")) {
                    is Result.Success<String> -> {
                        tempRouteInfo.add(LocationData(path[i], weather.data))
                    }

                    is Result.Error<String> -> {
                        Log.d("hail", "inside checkCondOfRoute" + weather.message)
                        return null
                    }
                }
            }
        }

        return tempRouteInfo
    }

    private fun distance(coord1: LatLng, coord2: LatLng): Int {

        val earthRadius = 6371000.0 //meters

        val dLat = Math.toRadians(coord2.latitude - coord1.latitude)
        val dLng = Math.toRadians(coord2.longitude - coord1.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(coord1.latitude)) * cos(
            Math.toRadians(coord2.latitude)
        ) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val dist = (earthRadius * c).toInt()

        return dist
    }

    private suspend fun getJSONObject(url: String): Result<JSONObject> {
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