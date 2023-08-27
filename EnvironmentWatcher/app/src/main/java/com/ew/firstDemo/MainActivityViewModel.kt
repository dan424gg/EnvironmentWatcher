package com.ew.firstDemo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ew.firstDemo.Location.LocationData
import com.ew.firstDemo.Location.LocationState
import com.ew.firstDemo.Location.NameToCoordinates
import com.ew.firstDemo.Location.RoutingClass
import com.ew.firstDemo.Weather.WeatherClass
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MainActivityViewModel() : ViewModel() {

    private val _locationState = MutableStateFlow(LocationState())
    val locationState: StateFlow<LocationState> = _locationState

    suspend fun getCurrentLocationInfo() {
        viewModelScope.launch {
            val location = LocationState()
            val coords = getLocation()
            val weatherInfo = getWeatherInfo(coords!!)
            location.curLocation = LocationData(coords, weatherInfo)
            location.isLoading = false
            delay(1000L)
            _locationState.emit(location)
        }
    }

    suspend fun getRoute(start: String, end: String) {
        val tempLocationState = _locationState.value.copy(
            isLoading = true, route = null
        )
        _locationState.emit(
            tempLocationState
        )

        lateinit var startCoordinates: LatLng
        if (start.isEmpty()) {
            startCoordinates = locationState.value.curLocation!!.coordinates!!
        } else {
            when (val result = NameToCoordinates.getCoordinates(start)) {
                is Result.Success -> {
                    startCoordinates = result.data!!
                }

                is Result.Error -> {
                    _locationState.emit(
                        tempLocationState.copy(
                            isLoading = false, error = result.message
                        )
                    )
                    return
                }
            }
        }

        lateinit var endCoordinates: LatLng
        if (end.isEmpty()) {
            _locationState.emit(
                tempLocationState.copy(
                    isLoading = false, error = "Don't forget to give your route a destination!"
                )
            )
            return
        } else {
            when (val result = NameToCoordinates.getCoordinates(end)) {
                is Result.Success -> {
                    endCoordinates = result.data!!
                }

                is Result.Error -> {
                    _locationState.emit(
                        tempLocationState.copy(
                            isLoading = false, error = result.message
                        )
                    )
                    return
                }
            }
        }

        viewModelScope.launch {
            _locationState.emit(
                tempLocationState.copy(
                    route = RoutingClass.getRouteInfo(startCoordinates, endCoordinates),
                    isLoading = false
                )
            )
        }
    }

    private suspend fun getWeatherInfo(coords: LatLng): String {
        val weather = WeatherClass.getWeatherData(coords, 0, "shortForecast")

        return suspendCancellableCoroutine { cont ->
            when (weather) {
                is Result.Success<String> -> {
                    cont.resume(weather.data!!)
                }

                is Result.Error<String> -> {
                    cont.resume(weather.message!!)
                }
            }
            return@suspendCancellableCoroutine
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getLocation(): LatLng? {
        // Use a client to access the location API
        val client = LocationServices.getFusedLocationProviderClient(MyApplication.appContext)

        return suspendCancellableCoroutine { cont ->

            client.lastLocation.apply {
                addOnSuccessListener {
                    cont.resume(LatLng(it.latitude, it.longitude))
                }
                addOnFailureListener {
                    cont.resume(null)
                }
                addOnCanceledListener {
                    cont.cancel()
                }
            }
        }
    }

    sealed class Result<T>(val data: T? = null, val message: String? = null) {
        class Success<T>(data: T?) : Result<T>(data)
        class Error<T>(message: String, data: T? = null) : Result<T>(data, message)
    }
}
