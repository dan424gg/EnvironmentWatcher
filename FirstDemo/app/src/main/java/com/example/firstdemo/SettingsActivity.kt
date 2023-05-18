package com.example.firstdemo

import android.Manifest
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreferenceCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        // PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val locPermissionListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if(key == "location" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                changeLocation()
                sharedPreferences.edit().putBoolean("location",
                    ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                ).apply()
            }
        }

        //PreferenceManager.getDefaultSharedPreferences(applicationContext)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    private fun changeLocation(){
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ), 1)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

}
