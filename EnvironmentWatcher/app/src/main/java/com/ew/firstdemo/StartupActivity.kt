package com.ew.firstdemo

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import com.ew.firstdemo.Location.CurrentLocation
import com.google.android.gms.maps.model.LatLng

class StartupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup2)
        val intent = Intent(this, MainActivity::class.java)

        CurrentLocation.getLocation(this) {
            val bundle = Bundle()

            // create a 'bundle' and hold a 'parcelable' that holds a LatLng object
            /* you need a parcelable to hold objects for sending in intents */
            bundle.putParcelable("curLocation", it)

            // put bundle in 'intent'
            intent.putExtra("curLocation", bundle)
            startActivity(intent)
            finish()
        }
    }
}