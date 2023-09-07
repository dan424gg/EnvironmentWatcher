package com.ew.firstdemo.Location

import com.google.android.gms.maps.model.LatLng

data class RouteData(
    var path: List<LatLng>? = null,
    var routeInfo: List<LocationData>? = emptyList()
)
