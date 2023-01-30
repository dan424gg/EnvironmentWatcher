package com.example.firstdemo.Location

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

class LocationViewModel: ViewModel() {

    private var _latitude: Double = 10.0
    private var _longitude: Double = 10.0
    private lateinit var _locationCallback : LocationCallback
    private lateinit var _locationRequest : LocationRequest

    val liveLongitude: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    val liveLatitude: MutableLiveData<Double> by lazy {
        MutableLiveData<Double>()
    }

    var latitude: Double
        get() = _latitude
        set(value: Double) {_latitude = value}

    var longitude: Double
        get() = _longitude
        set(value: Double) {_longitude = value}

    val locationCallback: LocationCallback
        get() = _locationCallback

    val locationRequest: LocationRequest
        get() = _locationRequest

    fun locationSetUp() {
        _locationRequest = LocationRequest.create()
        _locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        _locationRequest.interval = 20000

        _locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    longitude = location.longitude
                    latitude = location.latitude
                }
            }
        }
    }
}