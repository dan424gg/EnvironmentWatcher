package com.ew.firstdemo.Notifications

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper

class NotificationService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        TODO("Return the communication channel to the service. - only if we decide to use onbind ")
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val title = intent?.getStringExtra("title") ?: ""
        val content = intent?.getStringExtra("content") ?: ""
        val delayMins = intent?.getLongExtra("delay", 0) ?: 0L



        val runner = Runnable {
           //make notification here
        }

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(runner,1000*delayMins) //TODO: multiply by 60 after testing

        return START_STICKY

        return super.onStartCommand(intent, flags, startId)
    }
}