package com.rafdev.androidmobile.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.rafdev.androidmobile.databinding.ActivityMainBinding
import com.rafdev.androidmobile.main.viewpager.FirstFragment
import com.rafdev.androidmobile.main.viewpager.SecondFragment
import com.rafdev.androidmobile.main.viewpager.ThirdFragment
import com.rafdev.androidmobile.main.viewpager.adapter.ViewPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askNotificationPermission()
        viewPager()
    }

    private fun viewPager() {
        val viewPager = binding.viewPager
        val fragments = listOf(
            FirstFragment(),
            SecondFragment(),
            ThirdFragment()
        ) // Replace with your fragment instances
        val adapter = ViewPagerAdapter(this, fragments)
        viewPager.adapter = adapter

        val tabLayout = binding.tabLayout
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Set tab text or icon here if needed
            Log.i("viewPager2", "tab ---> $tab pos-->$position")
            tab.text = "Tab ${position + 1}"
        }.attach()
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

}