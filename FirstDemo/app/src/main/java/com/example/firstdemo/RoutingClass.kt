package com.example.firstdemo

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

import com.example.firstdemo.Weather.WeatherClass

object RoutingClass {

    private val client = OkHttpClient()
    private val path: MutableList<List<LatLng>> = ArrayList()
    private var totPath: List<LatLng> = ArrayList()
    private var pathForecast: MutableList<Pair<LatLng, String>> = ArrayList()
    private lateinit var json : JSONObject

    // Number of segments of route
    private var numSegments : Int = 6

    fun calling(googleMap: GoogleMap, origin: LatLng, destination: LatLng): MutableList<Pair<LatLng, String>> {

        // deconstruct LatLng objects to use in URL
        val originLat = origin.latitude
        val originLng = origin.longitude
        val destLat = destination.latitude
        val destLng = destination.longitude

        // construct URL
        val urlDirections =
            "https://maps.googleapis.com/maps/api/directions/json?origin=$originLat,$originLng&destination=$destLat,$destLng&key=AIzaSyCATUJigQ64LYkCQ5NkBYkaiSO8PWwL8ec"

        thread {
            json = JSONObject(run(urlDirections))
        }
        Thread.sleep(2000)
        getRoute(json, googleMap)

        return pathForecast
    }

    // Get routes
    private fun getRoute(json: JSONObject, googleMap: GoogleMap) : GoogleMap {

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

        segmentRoute(googleMap, duration)

        // On each segment of path, update UI map with path
        for (i in 0 until path.size) {
            googleMap.addPolyline(PolylineOptions().addAll(path[i]).color(
                android.graphics.Color.BLUE))
        }

        return googleMap
    }

    private fun segmentRoute(googleMap: GoogleMap, duration: Int) {

        // Convert cur time to seconds to get more accurate weather results later
        val curSeconds = curTimeToSeconds()

        // Flatten path list of LatLng objects
        totPath = path.flatten()

        // Find first segment of flattened path list, subtract one to not allow index go out of bounds (so segmentIdx != totPath.size)
        val segmentIdx = floorDiv(totPath.size, numSegments) - 1

        // Split time based on numSplits
        val segmentTime = floorDiv(duration, numSegments)

        for (i in 1 until numSegments) {
            // Add cur time to segmentTime
            val futureTime = (segmentTime * i) + curSeconds

            // Round futureTime to nearest hour
            val hour = round((futureTime / 60.0) / 60.0)

            val location = totPath[segmentIdx * i]
            val forecast = WeatherClass.calling(location)
            // Weatherclass.calling is sleeping for 3000
            pathForecast.add(Pair(location, forecast))
        }
    }

    private fun run(url : String) : String {
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw java.io.IOException("$response")
            return response.body!!.string()
        }
    }

    // Convert current time to seconds
    private fun curTimeToSeconds() : Double {

        val curTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME).split(":")
        val minutes = curTime[0].toFloat() * 60.0
        val curSeconds = (minutes * 60.0) + (curTime[1].toFloat() * 60.0) + curTime[2].toFloat()
        
        return curSeconds
    }
}
