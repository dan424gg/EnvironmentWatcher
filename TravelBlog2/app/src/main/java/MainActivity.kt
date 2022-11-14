package com.travelblog

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.travelblog.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainTextView.text = "This is my app"
        // activity is created
    }

    override fun onDestroy() {
        super.onDestroy()
        // activity is destroyed
    }
}