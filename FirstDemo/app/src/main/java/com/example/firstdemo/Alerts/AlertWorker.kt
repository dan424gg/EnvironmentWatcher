package com.example.firstdemo.Alerts

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.firstdemo.Location.CurrentLocation
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class AlertWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("ALERT", "entering the thingy")

        // Return Result.success() if the task is successful
        // Return Result.failure() if the task fails and should be retried
        // Return Result.retry() if the task fails but should be retried after some delay
        AlertPing.getAlertData(LatLng(CurrentLocation.latitude, CurrentLocation.longitude)) {response ->
            if (response != null) {

                Log.d("ALERT", "$response")
                AlertPing.AlertSend(response, applicationContext)

            } else {
                Log.d("ALERT", "null response")
            }


            Log.d("PLEASE", "Returning Success")
        }
        return Result.success()
    }
}
