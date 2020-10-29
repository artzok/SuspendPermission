package com.artzok.permission.app

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.artzok.permission.requestPermissions
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.location).setOnClickListener {
            lifecycleScope.launch {
                val result = requestPermissions(
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
                Log.d(TAG, "request location permissions:$result")
            }
        }
        findViewById<View>(R.id.record).setOnClickListener {
            lifecycleScope.launchWhenResumed {
                val result = requestPermissions(listOf(Manifest.permission.RECORD_AUDIO))
                Log.d(TAG, "request record permission:$result")
            }
        }
    }
}