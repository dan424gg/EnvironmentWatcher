package com.ew.firstdemo.Location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng

class CurrentLocationViewModel {
    private val _curLocation = MutableLiveData(LatLng(0.0, 0.0))
    val curLocation: LiveData<LatLng?> = _curLocation

    fun getLocation() {

    }
}