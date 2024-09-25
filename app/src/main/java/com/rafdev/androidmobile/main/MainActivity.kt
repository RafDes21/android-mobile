package com.rafdev.androidmobile.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.rafdev.androidmobile.databinding.ActivityMainBinding
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.UUID
import org.json.JSONException
import java.io.IOException
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var gso: GoogleSignInOptions? = null
    var gsc: GoogleSignInClient? = null

    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        askNotificationPermission()
        gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().build()
        gsc = GoogleSignIn.getClient(this, gso!!)
        initAuth()
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
        val deleteAuth = binding.deleteProfile


        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
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
                    Log.w("GoogleSignIn", "Google $idToken - $email - $displayName - $image")
                } catch (e: ApiException) {
                    Log.w("GoogleSignIn", "Google sign in failed", e)
                }
            }
        }

        deleteAuth.setOnClickListener {
            deleteProfile()
        }

        btnGoogle.setOnClickListener {
//            authGoogle()
            authGo()
        }
        btnFacebook.setOnClickListener {
            authFacebook()
        }
    }

    private fun authGo() {
        val signInIntent = gsc!!.signInIntent
        googleSignInLauncher.launch(signInIntent)
//        val signInIntent = gsc!!.signInIntent
//        startActivityForResult(signInIntent, 1000)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 1000) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = task.getResult(ApiException::class.java)
//                val idToken = account?.idToken
//                val email = account?.email
//                val displayName = account?.displayName
//                val image = account?.photoUrl
//                Picasso.get().load(image).into(binding.imageProfile)
//                binding.emailProfile.text = email
//                binding.nameProfile.text = displayName
//                Log.w("GoogleSignIn", "Google $idToken - $email - $displayName ")
//
//            } catch (e: ApiException) {
//                Log.w("GoogleSignIn", "Google sign in failed", e)
//            }
//        }
//    }

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

    private fun authFacebook() {
    }

    private fun authGoogle() {

        val credentialManager = CredentialManager.create(context = this)

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> "%02x".format(it) }

        val googleIdOptions = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId("283194243331-f7v1cpknlt6opbbllt5uool1o27udara.apps.googleusercontent.com")
            .setNonce(hashedNonce)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOptions)
            .build()

        lifecycle.coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = this@MainActivity
                )
                val credential = result.credential

//                Log.i("CREDENTIAL_TEST", "Credential Data: ${credential.data}")
//                Log.i("CREDENTIAL_TEST", "Credential Type: ${credential.type}")
//
//                val idToken = credential.data.getString("com.google.android.libraries.identity.googleid.BUNDLE_KEY_ID_TOKEN")
//                Log.i("CREDENTIAL_TEST", "ID Token: $idToken")
//
//                if (idToken != null) {
//                    fetchGoogleProfileInfo(idToken)
//                } else {
//                    Log.e("CREDENTIAL_TEST", "ID Token is null")
//                }

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val googleIdToken = googleIdTokenCredential.idToken

                Log.i("CREDENTIAL_TEST_GOO", "google: ${googleIdTokenCredential.data}")
                Log.i("CREDENTIAL_TEST_GOO", "google: ${googleIdTokenCredential.idToken}")
                Log.i("CREDENTIAL_TEST_GOO", "google: ${googleIdTokenCredential.familyName}")
                fetchGoogleProfileInfo(googleIdToken)

                Toast.makeText(this@MainActivity, "you are sig in", Toast.LENGTH_LONG).show()
            } catch (e: GetCredentialException) {
                Log.i("CREDENTIAL_TEST", "ErrCreden--> ${e.message}")
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            } catch (e: GoogleIdTokenParsingException) {
                Log.i("CREDENTIAL_TEST", "ErrGoogle--> ${e.message}")
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }

        }


    }

    private fun fetchGoogleProfileInfo(idToken: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(GoogleApiService::class.java)

        lifecycle.coroutineScope.launch {
            try {
                val response = service.getUserInfo("Bearer $idToken")
                if (response.isSuccessful) {
                    val userInfo = response.body()
                    Log.i(
                        "USER_INFO",
                        "Name: ${userInfo?.name}, Email: ${userInfo?.email}, Picture: ${userInfo?.picture}"
                    )
                } else {
                    Log.e(
                        "USER_INFO",
                        "Failed to fetch profile info: ${response.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("USER_INFO", "Error fetching profile info: ${e.message}")
            }
        }
    }
}

interface GoogleApiService {
    @GET("oauth2/v2/userinfo")
    suspend fun getUserInfo(@Header("Authorization") authHeader: String): retrofit2.Response<UserInfo>
}

data class UserInfo(
    val name: String,
    val email: String,
    val picture: String
)
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == 1000) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleSignInResult(task)
//        }
//    }
//
//    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
//        try {
//            val account = completedTask.getResult(ApiException::class.java)
//            Log.w("GoogleSignIn", "Google Sign-In successful: ${account?.email}")
//            // Continúa con el flujo de autenticación
//        } catch (e: ApiException) {
//            Log.w("GoogleSignIn", "Google Sign-In failed: $e")
//        }
//    }

//    private fun authGoogle() {
//        val googleIdOption = GetGoogleIdOption.Builder()
//            .setFilterByAuthorizedAccounts(true)
//            .setServerClientId("283194243331-k65bokk4s1h4cgumvsufumlcvj4d9h63.apps.googleusercontent.com")
//            .setAutoSelectEnabled(false)
//            .setNonce(generateNonce())
//            .build()
//
//        val request = GetCredentialRequest.Builder()
//            .addCredentialOption(googleIdOption)
//            .build()
//
//
//        lifecycleScope.launch {
//            try {
//                val credentialManager = CredentialManager.create(this@MainActivity)
//                val result = credentialManager.getCredential(
//                    request = request,
//                    context = this@MainActivity
//                )
//                Log.w("GoogleSignIn", "Google ${result.credential} ")
//                handleSignIn(result)
//            } catch (e: Exception) {
//                handleError(e)
//                Log.w("GoogleSignIn", "Error $e")
//
//            }
//        }
//
//    }
//
//    private fun generateNonce(): String {
//        return UUID.randomUUID().toString()
//    }
//
//    private fun handleSignIn(result: GetCredentialResponse) {
//        Log.w("GoogleSignIn", "Google $result ")
//        val credential = result.credential
//        when (credential) {
//            is CustomCredential -> {
//
//            }
//        }
//
//    }
//
//    private fun handleError(exception: Exception) {
//        when (exception) {
//            is androidx.credentials.exceptions.GetCredentialCancellationException -> {
//                Log.w("GoogleSignIn", "La autenticación fue cancelada por el usuario.")
//            }
//            else -> {
//                Log.w("GoogleSignIn", "Error: $exception")
//            }
//        }
//    }
//
//    private fun deleteProfile() {
//        binding.deleteProfile.setOnClickListener {
//            lifecycleScope.launch {
//                try {
//                    val credentialManager = CredentialManager.create(this@MainActivity)
//                    credentialManager.clearCredentialState(request = androidx.credentials.ClearCredentialStateRequest())
//                    Log.w("GoogleSignIn", "Estado de credenciales limpiado exitosamente")
//                } catch (e: Exception) {
//                    Log.w("GoogleSignIn", "Error al limpiar el estado de las credenciales: $e")
//                    handleError(e)
//                }
//            }
//        }
//    }

//    private fun authGoogle() {
//        gso =
//            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
//
//        gsc = GoogleSignIn.getClient(this, gso!!)
//
//        val signInIntent = gsc!!.signInIntent
//        startActivityForResult(signInIntent, 1000)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 1000) {
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = task.getResult(ApiException::class.java)
//                val idToken = account?.idToken
//                val email = account?.email
//                val displayName = account?.displayName
//                val image = account?.photoUrl
//                Picasso.get().load(image).into(binding.imageProfile)
//                binding.emailProfile.text = email
//                binding.nameProfile.text = displayName
//                Log.w("GoogleSignIn", "Google $idToken - $email - $displayName ")
//
//            } catch (e: ApiException) {
//                Log.w("GoogleSignIn", "Google sign in failed", e)
//            }
//        }
//    }
//
//
//    private fun deleteProfile() {
//        binding.deleteProfile.setOnClickListener {
//            gsc?.signOut()?.addOnCompleteListener(this) {
//                if (it.isSuccessful) {
//                    binding.emailProfile.text = ""
//                    binding.nameProfile.text = ""
//                    binding.imageProfile.setImageDrawable(null)
//                    Log.w("GoogleSignOut", "Successfully signed out from Google")
//                } else {
//                    Log.w("GoogleSignOut", "Failed to sign out from Google", it.exception)
//                }
//            }
//        }
//    }


//val acct = GoogleSignIn.getLastSignedInAccount(this)
