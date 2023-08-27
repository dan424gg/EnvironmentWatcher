package com.ew.firstDemo.Weather

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.ew.firstDemo.MyApplication
import com.ew.firstDemo.R

class WeatherParser(val weatherInfo: String, val somthing: Context) {
    val context = MyApplication.appContext
    //stored values
    var img: Bitmap

    init {
        Log.d("Aidan", "Making weather parser object")
        img = findWeatherType(weatherInfo, context)
    }

    private fun findWeatherType(weatherString: String, context: Context): Bitmap {
        //list of all keywords (Thanks leanne!), in order of priority for reg expression
        //comments cover order (for now). may be shifted around.
        lateinit var bitmap: Bitmap

        val substrings = listOf(

            //appears once and only once, don't even worry about it
            "Thunderstorm Small Hail",
            "Thunderstorm Hail",
            "Thunderstorm Heavy Rain",
            "Thunderstorm Light Rain",
            "Thunderstorm Showers",
            "Thunderstorm Rain",
            "Showers and Thunderstorms",
            "Clear",
            "Funnel Cloud",
            "Tornado",


            //after thunderstorm variants, and snow
            "Thunderstorm",


            //after haze, windy
            "Overcast",
            "Smoke",
            "Freezing Rain",
            "Freezing Drizzle",
            "Drizzle",
            "Ice Pellets",
            "Ice Crystals",

            //after thunderstorm
            "Snow",
            "Windy",

            "Light Rain",
            //after light rain, thunderstorm,
            "Rain",
            "Showers",


            "Dust",
            "Sand",

            //after thunderstorm
            "Haze",

            //after windy and haze
            "Fair",

            //after basically everything
            "Fog",
            "Cloud",
            "Breezy",
            "Sunny"
        )

        val badWeather = mapOf(

            "Thunderstorm Small Hail" to R.drawable.thunderstorm_rain,
            "Thunderstorm Hail" to R.drawable.thunderstorm_rain,
            "Thunderstorm Heaver Rain" to R.drawable.thunderstorm_rain,
            "Thunderstorm Light Rain" to R.drawable.thunderstorm_rain,
            "Thunderstorm Showers" to R.drawable.thunderstorm_rain,
            "Thunderstorm Rain" to R.drawable.thunderstorm_rain,
            "Showers and Thunderstorms" to R.drawable.thunderstorm_rain,

            "Thunderstorm" to R.drawable.thunderstorm,
            "T-storms" to R.drawable.thunderstorm,

            "Fair" to R.drawable.sun,
            "Clear" to R.drawable.sun,
            "Sunny" to R.drawable.sun,

            "Cloud" to R.drawable.partly_cloudy,

            "Overcast" to R.drawable.cloud,

            "Fog" to R.drawable.fog,
            "Smoke" to R.drawable.fog,
            "Haze" to R.drawable.fog,

            "Freezing Rain" to R.drawable.snow,
            "Freezing Drizzle" to R.drawable.snow,
            "Ice Pellets" to R.drawable.snow,
            "Ice Crystals" to R.drawable.snow,
            "Snow" to R.drawable.snow,

            "Light Rain" to R.drawable.light_rain,
            "Drizzle" to R.drawable.light_rain,

            "Showers" to R.drawable.heavy_rain,
            "Rain" to R.drawable.heavy_rain,

            "Breezy" to R.drawable.wind,
            "Windy" to R.drawable.wind,

            "Tornado" to R.drawable.tornado,
            "Funnel Cloud" to R.drawable.tornado,

            "Dust" to R.drawable.dust_sand,
            "Sand" to R.drawable.dust_sand,
        )

        var iconType: String? = null
        for (strings in substrings) {
            iconType = strings.toRegex().find(weatherString)?.value
            if (iconType != null) {
                break
            }
        }

        //val regex = substrings.joinToString("|").toRegex()

        //iconType is a matchResult object
        //val iconType = regex.find(weatherString)?.value
        Log.d("Aidan", "weatherString: $weatherString")

        if (iconType == null) {
            Log.d("Aidan", "iconType is null, using default")
            return BitmapFactory.decodeResource(context.resources, R.drawable.unknown)
        } else {
            Log.d("Aidan", "iconType: $iconType")
        }

        val image = badWeather[iconType]
        //maybe add a trycatch for out of memory error, in case images are too big
        bitmap =
            if (image != null) {
                BitmapFactory.decodeResource(context.resources, image)
            } else {
                /* If you see this log, either:
                        -the regex didn't find the substring
                        -the input substring was wrong
                        -the hashmap sucks
                       hopefully this will never appear, but if it does do more testing
                     */
                Log.d("weatherObject", "bitmap not made");
                BitmapFactory.decodeResource(context.resources, R.drawable.unknown)
        }


        return bitmap

    }

}
