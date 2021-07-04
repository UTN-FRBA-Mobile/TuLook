package com.example.tulook

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.children
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.tulook.databinding.ActivityMainBinding
import com.example.tulook.fileSystem.MyPreferenceManager
import com.example.tulook.services.APIService
import com.example.tulook.services.MyFirebaseMessagingService
import com.example.tulook.services.getToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import okhttp3.Callback
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import java.io.IOException


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    var drawerLayout: DrawerLayout? = null
    var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    // auth
    private var auth: FirebaseAuth? = null
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val RC_SIGN_IN = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TuLook)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Este nav controller tiene a cargo y conoce toda la navegación de nuestra app
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        drawerLayout = binding.drawerLayout

        setupDrawerLayout()

        // TODO temp, para mandar notificaciones desde consola
        Log.d("token", getToken(this) ?: "empty")

        // auth
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    private fun setupDrawerLayout() {
        binding.navView.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        updateAuthButton()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        updateAuthButton()
    }

    // https://stackoverflow.com/questions/34973456/how-to-change-text-of-a-textview-in-navigation-drawer-header
    private fun updateAuthButton() {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val menu = navigationView.menu
        val loginItem = menu.findItem(R.id.loginAction)

        val isLoggedIn = auth?.currentUser != null

        Log.d("auth", "updating auth button, isLoggedIn: $isLoggedIn")

        if (isLoggedIn) {
            loginItem?.title = "Cerrar Sesión"
            loginItem.setOnMenuItemClickListener { startSignout() }
        } else {
            loginItem?.title = "Iniciar Sesión"
            loginItem.setOnMenuItemClickListener { startSignin() }
        }
    }

    // auth
    private fun startSignin(): Boolean {
        val signInIntent = mGoogleSignInClient?.signInIntent
        Log.d("auth", "$signInIntent, starting signin")
        if (signInIntent != null) startActivityForResult(signInIntent, RC_SIGN_IN)
        return true
    }

    private fun startSignout(): Boolean {
        val user = auth?.currentUser

        if (user == null) return true

        updateBackend(user.uid, user.displayName!!, null)
        auth?.signOut()
        MyPreferenceManager.clearUser(this)
        updateAuthButton()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("auth", "firebaseAuthWithGoogle:" + account.id)
                Toast.makeText(this, account.id, Toast.LENGTH_LONG).show()
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("auth", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth?.signInWithCredential(credential)?.addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d("auth", "signInWithCredential:success")
                val user = auth?.currentUser
                if (user != null) MyPreferenceManager.setUser(this, user.displayName!!, user.photoUrl.toString())
                updateAuthButton()
                updateBackend(
                    user!!.uid,
                    user.displayName!!,
                    getToken(this)!!
                )
            } else {
                // If sign in fails, display a message to the user.
                Log.w("auth", "signInWithCredential:failure", task.exception)
            }
        }
    }

    private fun updateBackend(uid: String, name: String, notificationToken: String?) {
        val gson = GsonBuilder().create()

        val data = UserData(
            uid, name, notificationToken
        )

        val body = gson.toJsonTree(data).asJsonObject

        APIService.create().login(body).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("auth", "onresponse")
//                TODO("Not yet implemented")
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d("auth", "onfailure")
//                TODO("Not yet implemented")
            }
        })
    }
}

data class UserData(
    @SerializedName("id") val id: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("notificationToken") val notificationToken: String?)

