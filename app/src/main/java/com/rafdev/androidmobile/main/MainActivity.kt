package com.rafdev.androidmobile.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import com.rafdev.androidmobile.R
import com.rafdev.androidmobile.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askNotificationPermission()
        testProgress()
    }

    private fun testProgress() {
//        binding.progressHorizontalOne.progress = 30
        binding.progressHorizontalTow.progress = 30
        binding.contentOne.setOnClickListener {
            binding.textOne.isSelected = true
            binding.progressHorizontalOne.progressDrawable =
                ContextCompat.getDrawable(this, R.drawable.custom_progress_inactive)

        }
        val runnable = object : Runnable {
            var progress = 10
            override fun run() {
                if (progress < 100) {
                    progress += 10
                    binding.progressHorizontalOne.progress = progress
                    handler.postDelayed(this, 2 * 1000)
                } else {
                    binding.progressHorizontalOne.progress = 0
                    progress = 10
                    handler.postDelayed(this, 2 * 1000)
                }
            }
        }
        handler.post(runnable)
    }


    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {

            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {

        } else {

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

}