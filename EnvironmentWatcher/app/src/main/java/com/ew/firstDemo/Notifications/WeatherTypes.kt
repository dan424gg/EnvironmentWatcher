package com.ew.firstDemo.Notifications

import com.ew.firstDemo.R

enum class WeatherTypes(val type: String) {

    RAIN("rain") {
        val smallIcon = R.drawable.ic_rain
        val title = "rain"
        val text = "it gonna rain"

    },

    SUN("sun") {
        val smallIcon = R.drawable.ic_sun
        val title = "sun"
        val text = "it gonna sun"

    },

    OTHER("other") {

    }


    //else constructor
}