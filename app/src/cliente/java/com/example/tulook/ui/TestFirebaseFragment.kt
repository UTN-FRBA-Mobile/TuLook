package com.example.tulook.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.tulook.R
import com.example.tulook.databinding.FragmentTestFirebaseBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class TestFirebaseFragment : Fragment() {
    private var _binding: FragmentTestFirebaseBinding? = null
    private val binding get() = _binding!!

    // auth
    private lateinit var auth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1

    // map
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val RC_LOCATION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // auth
        auth = Firebase.auth

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // map
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getLocation()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTestFirebaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser
        if (user != null)
            Log.d("auth", "firebaseAuthWithGoogle:" + user.displayName)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // auth
        val btnSignIn = binding.btnSignIn

        btnSignIn.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        // map
        val btnRetryLocation = binding.btnRetryLocation
        btnRetryLocation.setOnClickListener { getLocation() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RC_LOCATION) getLocation()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.d("auth", "firebaseAuthWithGoogle:" + account.id)
                Toast.makeText(context, account.id, Toast.LENGTH_LONG).show()
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("auth", "Google sign in failed", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity!!) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("auth", "signInWithCredential:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("auth", "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                activity!!,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val perms = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            ActivityCompat.requestPermissions(activity!!, perms, RC_LOCATION)
            return
        }

        Log.d("LOCATION", "Registering location onsuccesslistener")
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            Log.d("LOCATION", location.toString())
            if (location != null) {
                val mapFragment = childFragmentManager.findFragmentByTag("test_map") as SupportMapFragment
                mapFragment.getMapAsync { map ->
                    val marker = MarkerOptions()
                    val position = LatLng(location.latitude, location.longitude)
                    marker.position(position)
                    map.addMarker(marker)


                    map.moveCamera(CameraUpdateFactory.zoomTo(16.0F))
                    map.moveCamera(CameraUpdateFactory.newLatLng(position))
                }

                Log.d("LOCATION", "lat: ${location.latitude}, lng: ${location.longitude}")
            } else
                Log.d("LOCATION", "Location was null.")
        }
    }
}
