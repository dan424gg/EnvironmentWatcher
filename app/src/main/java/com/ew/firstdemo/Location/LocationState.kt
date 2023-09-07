package com.ew.firstdemo.Location

data class LocationState(
    var curLocation: LocationData? = null,
    var route: RouteData? = null,
    var isLoading: Boolean = true,
    var error: String? = null
)
