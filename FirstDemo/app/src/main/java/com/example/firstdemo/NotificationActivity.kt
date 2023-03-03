package com.example.firstdemo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationActivity : AppCompatActivity() {
    private var sent = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        var builder = NotificationCompat.Builder(this, "0")
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("TESTING")
            .setContentText("The fog consumes us all")
            //.setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        createNotificationChannel()
        getNotificationPermission()

        with(NotificationManagerCompat.from(this)) {

            if (ActivityCompat.checkSelfPermission(
                    this@NotificationActivity,
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
                getNotificationPermission()

                return
            }
            // notificationId (first arg) is a unique int for each notification that you must define
            notify(1, builder.build())

            val returnIntent = Intent(this@NotificationActivity, MainActivity::class.java)
            finish()
        }

    }

    private fun getNotificationPermission() {
        Log.d("Aidan", "entering getNotificationPermission")
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)

    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)

            //Importance value determines if notification should replace last notification from same channel id
            //set priority with setPriority()
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("0", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    //parameters: unique channel id, user-visible name, user-visible description, use notification.IMPORTANCE_x for int
    public fun makeNotificationChannel(ID : String, channelName : String, descriptionText : String, importance : Int){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(ID, channelName, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    public fun makebuilder() {

    }
}