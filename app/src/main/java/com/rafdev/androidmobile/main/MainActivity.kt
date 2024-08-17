package com.rafdev.androidmobile.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.rafdev.androidmobile.R
import com.rafdev.androidmobile.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var gso: GoogleSignInOptions? = null
    var gsc: GoogleSignInClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askNotificationPermission()

        initAuth()
        deleteProfile()
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


    private fun initAuth() {
        val btnGoogle = binding.icGoogle
        val btnFacebook = binding.icFacebook
        btnGoogle.setOnClickListener {
            authGoogle()
        }
        btnFacebook.setOnClickListener {
            authFacebook()
        }
    }

    private fun authFacebook() {
    }


    private fun authGoogle() {
        gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()

        gsc = GoogleSignIn.getClient(this, gso!!)

        val signInIntent = gsc!!.signInIntent
        startActivityForResult(signInIntent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                val email = account?.email
                val displayName = account?.displayName
                val image = account?.photoUrl
                Picasso.get().load(image).into(binding.imageProfile)
                binding.emailProfile.text = email
                binding.nameProfile.text = displayName
                Log.w("GoogleSignIn", "Google $idToken - $email - $displayName ")

            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
            }
        }
    }


    private fun deleteProfile() {
        binding.deleteProfile.setOnClickListener {
            gsc?.signOut()?.addOnCompleteListener(this) {
                if (it.isSuccessful) {
                    binding.emailProfile.text = ""
                    binding.nameProfile.text = ""
                    binding.imageProfile.setImageDrawable(null)
                    Log.w("GoogleSignOut", "Successfully signed out from Google")
                } else {
                    Log.w("GoogleSignOut", "Failed to sign out from Google", it.exception)
                }
            }
        }
    }

}

//val acct = GoogleSignIn.getLastSignedInAccount(this)
