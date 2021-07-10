package com.example.tulook

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.tulook.databinding.ActivityMainBinding
import com.example.tulook.fileSystem.MyPreferenceManager
import com.example.tulook.services.APIService
import com.example.tulook.services.getToken
import com.example.tulook.util.CircleTransform
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    var drawerLayout: DrawerLayout? = null
    var actionBarDrawerToggle: ActionBarDrawerToggle? = null

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

        // auth
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        auth?.addAuthStateListener { updateAuthStatus() }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, drawerLayout)
    }

    private fun setupDrawerLayout() {
        binding.navView.setupWithNavController(navController)
        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        updateAuthStatus()
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
        updateAuthStatus()
    }

    private fun updateAuthStatus() {
        updateAuthButton()
        updateAuthHeader()
    }

    // https://stackoverflow.com/questions/34973456/how-to-change-text-of-a-textview-in-navigation-drawer-header
    private fun updateAuthButton() {
        val user = MyPreferenceManager.getUser(this)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val menu = navigationView.menu
        val loginItem = menu.findItem(R.id.loginAction)

        if (user != null) {
            loginItem?.title = "Cerrar Sesión"
            loginItem.setOnMenuItemClickListener { startSignout() }
        } else {
            loginItem?.title = "Iniciar Sesión"
            loginItem.setOnMenuItemClickListener { startSignin() }
        }
    }

    private fun updateAuthHeader() {
        val user = MyPreferenceManager.getUser(this)

        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val header = navigationView.getHeaderView(0)
        val headerText = header.findViewById<TextView>(R.id.nav_header_textView)
        val headerImage = header.findViewById<ImageView>(R.id.nav_header_imageView)

        Log.d("auth", "updating header, user is $user, headerImage is $headerImage")

        if (user == null) {
            headerText?.text = "TuLook"
            headerImage?.setImageResource(R.mipmap.ic_init)
        } else {
            if (headerImage == null) return

            headerText?.text = user.displayName

            Picasso.get()
                .load(Uri.parse(user.photoUrl))
                .transform(CircleTransform())
                .fit()
                .centerCrop()
                .into(headerImage)
        }
    }

    // auth
    private fun startSignin(): Boolean {
        val signInIntent = mGoogleSignInClient?.signInIntent
        if (signInIntent != null) { startActivityForResult(signInIntent, RC_SIGN_IN) }
        return true
    }

    private fun startSignout(): Boolean {
        val user = auth?.currentUser ?: return true

        updateBackend(user.uid, user.displayName!!, null)
        auth?.signOut()
        MyPreferenceManager.clearUser(this)
        updateAuthStatus()
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
                if (user != null) {
                    Log.d("auth", "setting user, $user")
                    MyPreferenceManager.setUser(this, user.displayName!!, user.photoUrl.toString())
                    Log.d("auth", "Saved value: ${MyPreferenceManager.getUser(this)}")
                }
                updateAuthStatus()
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

