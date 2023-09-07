package com.ew.firstdemo.Location

import com.google.android.gms.maps.model.LatLng

data class LocationData(
    var coordinates: LatLng? = null,
    var weather: String? = null,
)
