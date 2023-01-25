package com.example.firstdemo

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : AppCompatActivity() {
    private var latitude = 0.0
    private var longitude = 0.0
    
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val lat: TextView = findViewById(R.id.latitude)
        val long: TextView = findViewById(R.id.longitude)
        val button: Button = findViewById(R.id.loc_button)

        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if(it.resultCode == Activity.RESULT_OK){
                // Handle result data
                if(it.data != null) {
                    Log.d("DEBUG", "BACK TO MAIN")
                    Log.d("DEBUG", it.data!!.getDoubleExtra("latitude", -1.0).toString())
                    latitude = it.data!!.getDoubleExtra("latitude", latitude)
                    longitude = it.data!!.getDoubleExtra("longitude", longitude)
                }
            }
        }

        val startIntent = Intent(this, LocationActivity::class.java).also{
            it.putExtra("latitude", latitude)
            it.putExtra("longitude", longitude)
        }

        button.setOnClickListener{
            startForResult.launch(startIntent)
            lat.text = latitude.toString()
            long.text = longitude.toString()
        }
    }
}