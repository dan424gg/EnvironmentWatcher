package com.ew.firstDemo.Alerts

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ew.firstDemo.MainActivity
import com.ew.firstDemo.R
import com.google.android.gms.maps.model.LatLng
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

object AlertPing {
    private val okHttpClient = OkHttpClient()

    fun getAlertData(location: LatLng?, callback: (result: String?) -> Unit) {
        val latitude = location?.latitude
        val longitude = location?.longitude

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

    fun AlertSend(response: String?, context: Context, debugflag: Int = 0) {
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
                    val event = currentProperties.getString("event")
                    val description = currentProperties.getString("description")
                    val icon = alertParser(event, context)

                    //make the notification
                    val notificationBuilder = NotificationCompat.Builder(context, "alerts")
                        .setSmallIcon(icon)
                        .setContentTitle(event)
                        .setContentText(description)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        //.setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setContentIntent(PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                        .setAutoCancel(true)

                    //make a notification manager
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(1, notificationBuilder.build())
                }
            }
        }
    }

    private fun alertParser(input: String, context: Context): Int  {

        val substrings = listOf(
            "Ashfall",
            "Avalanche",
            "Blizzard",
            "Ice Storm",
            "Snow",
            "Winter",
            "Dust",
            "Wind",
            "Fog",
            "Smoke",
            "Heat",
            "Cold",
            "Freeze",
            "Fire",
            "Flood",
            "Hurricane",
            "Storm",
            "Tornado",
            "Sunny",
        )

        val alertImgs = mapOf(
            "Ash" to R.drawable.volcano,

            "Avalanche" to R.drawable.avalanche,

            "Blizzard" to R.drawable.snow,
            "Ice Storm" to R.drawable.snow,
            "Snow" to R.drawable.snow,
            "Winter" to R.drawable.snow,

            "Dust" to R.drawable.dust_sand,

            "Wind" to R.drawable.wind_warning,

            "Fog" to R.drawable.fog,
            "Smoke" to R.drawable.fog,

            "Heat" to R.drawable.hot,

            "Cold" to R.drawable.cold,
            "Freeze" to R.drawable.cold,

            "Fire" to R.drawable.fire,

            "Flood" to R.drawable.flood,

            "Hurricane" to R.drawable.hurricane,

            "Storm" to R.drawable.bad_thunderstorm,

            "Tornado" to R.drawable.tornado,

        )

        val regex = substrings.joinToString(separator = "|")
        val iconType: String = regex.toRegex().find(input)?.value
            //?: return BitmapFactory.decodeResource(context.resources, R.drawable.user_icon)
            ?: return R.drawable.user_icon

        val image = alertImgs[iconType]

        if (image != null)  {
            return image
        }

        return R.drawable.user_icon
        /*
        val image = alertImgs[iconType]

        val bitmap: Bitmap =
            if (image != null) {
                BitmapFactory.decodeResource(context.resources, image)
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.user_icon)
            }

        return bitmap
        */
    }

}