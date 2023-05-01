package com.example.firstdemo.Alerts

import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.concurrent.thread

object AlertPing {
    private val okHttpClient = OkHttpClient()

    fun getAlertData(location: LatLng, hour: Int = 0, property: String = "", callback: (result: String?) -> Unit) {
        val latitude = location.latitude
        val longitude = location.longitude

        lateinit var json : JSONObject

        Thread {
            val request = Request.Builder()
                .url("https://api.weather.gov/alerts/active?point=$latitude,$longitude")
                .header("User-agent", "an agent")
                .build()

        try {
                val response = okHttpClient.newCall(request).execute()

                if(response.isSuccessful && response.body?.string() != null) {
                    callback(response.body?.string())
                } else {
                    callback(null)
                }
                response.close()
        } catch (e: Exception) {
            callback(null)
        }
        }
    }
}