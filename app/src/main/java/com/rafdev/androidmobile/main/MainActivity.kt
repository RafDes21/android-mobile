package com.rafdev.androidmobile.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayoutMediator
import com.rafdev.androidmobile.R
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
        )
        val adapter = ViewPagerAdapter(this, fragments)
        viewPager.adapter = adapter

        val tabLayout = binding.tabLayout
        val tabTitles = listOf(" Mis Datos ", " Mis Notificaciones ", " Información Legal ")

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

//        val tabLayout = binding.tabLayout
//        val tabTitles = listOf("Mis Datos", "Mis Notificaciones", "Información Legal")
//
//        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
//            val tabView = LayoutInflater.from(tabLayout.context).inflate(R.layout.custom_tab, null)
//            val tabTitle = tabView.findViewById<TextView>(R.id.tabTitle)
//            tabTitle.text = tabTitles[position]
//            tab.customView = tabView
//        }.attach()

//        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
//            tab.text = tabTitles[position]
//            if (position == 0) {
//                val layoutParams = LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT
//                )
//                layoutParams.width = resources.getDimensionPixelSize(R.dimen.tab_width_short)
//                tab.view.layoutParams = layoutParams
//            }
//        }.attach()
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