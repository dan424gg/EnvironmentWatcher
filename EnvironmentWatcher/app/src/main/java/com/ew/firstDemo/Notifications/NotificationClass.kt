package com.ew.firstDemo

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.util.Log

import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationClass {

    fun calling(that: Activity) {
        Log.d("aidan", "entering NotificationActivity")

        // commented code below (and .setContentIntent() below) will allow notification to be interactive)
        /*
        val intent = Intent(this, AlertDetails::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        */

        //2nd argument is a channel id
        //https://stackoverflow.com/questions/58526610/what-channelid-should-i-pass-to-the-constructor-of-notificationcompat-builder

        var builder = NotificationCompat.Builder(that, R.string.channel_name.toString())
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("TESTING")
            .setContentText("The fog consumes us all")
            //.setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        makeChannel(that, NotificationManager.IMPORTANCE_HIGH)
        getNotificationPermission(that, 1)

        with(NotificationManagerCompat.from(that)) {

            if (ActivityCompat.checkSelfPermission(
                    that,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("aidan", "notification settings not enabled")
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                getNotificationPermission(that, 2)

                return
            }
            // notificationId (first arg) is a unique int for each notification that you must define
            Log.d("Aidan", "sending notification")
            notify(1, builder.build())

        }

    }

    private fun getNotificationPermission(that: Activity, code : Int) {
        Log.d("Aidan", "entering getNotificationPermission")
        ActivityCompat.requestPermissions(that, arrayOf(Manifest.permission.POST_NOTIFICATIONS), code)
    }


    fun makeChannel(that: Activity, importance: Int ) {
        //Create the NotificationChannel, but only on API 26+ because
        //the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = R.string.channel_name
            val descriptionText = R.string.channel_description

            //Importance value determines if notification should replace last notification from same channel id
            //set priority with setPriority()
            val channel = NotificationChannel("0", name.toString(), importance).apply {
                description = descriptionText.toString()
            }
            // Register the channel with the system

            val manager = that.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            return manager.createNotificationChannel(channel)
        }
    }

    /* up default notification channel
    NotificationClass.makeNotificationChannel(
    this, "default", getString(R.string.WeatherUpdateChannelName),
    "Current weather", 3
    )

     //call notification for test
                    NotificationClass.sendNotification(this, weather, "weather description", getWeatherImage(weather))

    */
    //parameters: context, unique channel id, user-visible name, user-visible description, use notification.IMPORTANCE_x for int
    public fun makeNotificationChannel(that: Activity, ID : String, channelName : String, descriptionText : String, importance : Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(ID, channelName, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                that.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }



    public fun sendNotification(that : Activity, title : String, description: String, bits : Bitmap) {
        var builder = NotificationCompat.Builder(that, "default")
            .setSmallIcon(R.drawable.sunny)
            .setLargeIcon(bits)
            .setContentTitle(title)
            .setContentText(description)
            //.setContentIntent(pendingIntent)
            //.setPriority()
            //.setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                that,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        //notification id should be incremented, in order to make notifications unique.
        NotificationManagerCompat.from(that).notify(1, builder.build())
    }
}