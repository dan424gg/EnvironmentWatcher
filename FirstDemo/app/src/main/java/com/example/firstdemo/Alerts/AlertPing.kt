package com.example.firstdemo.Alerts

import android.app.Activity
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlin.concurrent.thread

object AlertPing {
    private val okHttpClient = OkHttpClient()

    fun getAlertData(location: LatLng, that: Activity, callback: (result: String?) -> Unit) {
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

                callback(response.body?.string())


                response.close()
            } catch (e: Exception) {
                callback(null)
            }
        }.start()
    }

    fun AlertParse(response: String?, that: Activity) {
        if (response != null) {
            val features = JSONObject(response).getJSONArray("features")

            if (features.length() == 0) {
                Log.d("ALERT", "No alerts right now")
            } else {
                //make a notification
                Log.d("ALERT", "Alert Found")
                for (i in 0 until features.length()) {
                    //get the json info
                    val currentProperties = features.getJSONObject(i).getJSONObject("properties")
                    val event = currentProperties.getJSONObject("event")
                    val description = currentProperties.getString("description")

                    /*
                    //make the notifications
                    val notificationBuilder = NotificationCompat.Builder(that, "alerts")
                    //.setSmallIcon(...)
                        .setContentTitle("It's Alive!!!")
                        .setContentText(alertDescription)
                    */
                }
            }
        }
    }
}