package com.ew.firstdemo.Location

import android.location.Geocoder
import android.util.Log
import com.ew.firstdemo.MainActivityViewModel.Result
import com.ew.firstdemo.MyApplication
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object NameToCoordinates {

    suspend fun getCoordinates(
        location: String
    ): Result<LatLng?> {
        val activity = MyApplication.appContext
        val geocoder = Geocoder(activity)

        return suspendCancellableCoroutine { cont ->
            try {
                geocoder.getFromLocationName(
                    location, 1
                ) { result ->
                    if (result.isEmpty()) {
                        cont.resume(Result.Error("Location not found! Check your locations!"))
                    } else if (!result[0].countryCode.equals("US")) {
                        cont.resume(Result.Error("Location has to be in the US!"))
                    } else {
                        cont.resume(
                            Result.Success(
                                LatLng(result[0].latitude, result[0].longitude)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.d("hail", "error in NameToCoordinates/getCoordinates: $e")
            }
        }
    }
}
