package com.ew.firstDemo.Location

import android.location.Geocoder
import android.os.Build
import androidx.annotation.RequiresApi
import com.ew.firstDemo.MainActivityViewModel.Result
import com.ew.firstDemo.MyApplication
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object NameToCoordinates {

    // Returns the first city that most closely matches the user's input
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    suspend fun getCoordinates(
        location: String
    ): Result<LatLng?> {
        val activity = MyApplication.appContext
        val geocoder = Geocoder(activity)

        return suspendCancellableCoroutine { cont ->
            geocoder.getFromLocationName(
                location, 1
            ) { result ->
                if (!result[0].countryCode.equals("US")) {
                    cont.resume(Result.Error<LatLng?>("Location has to be in the US!"))
                } else {
                    cont.resume(Result.Success(
                        LatLng(result[0].latitude, result[0].longitude)
                    ))
                }
            }
        }
    }
}
