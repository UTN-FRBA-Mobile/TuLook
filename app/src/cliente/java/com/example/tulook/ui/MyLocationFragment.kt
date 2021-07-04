package com.example.tulook.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.tulook.databinding.FragmentMyLocationBinding
import com.example.tulook.fileSystem.LocationStorage
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MyLocationFragment : Fragment() {
    private var _binding: FragmentMyLocationBinding? = null
    private val binding get() = _binding!!

    private lateinit var geocoder: Geocoder
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val RC_LOCATION = 2

    private var savedLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        geocoder = Geocoder(requireContext())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        getLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnRetryLocation = binding.btnRetryLocation
        btnRetryLocation.setOnClickListener { getLocation() }

        val btnSaveLocation = binding.btnSaveLocation
        btnSaveLocation.setOnClickListener {
            val location = savedLocation
            if (location != null) updateStoredLocation(location)
        }

        val mapFragment = childFragmentManager.findFragmentByTag("test_map") as SupportMapFragment

        mapFragment.getMapAsync { map ->
            map.setOnMapClickListener {
                val target = Location("")
                target.latitude = it.latitude
                target.longitude = it.longitude

                updateLocation(target)
            }
        }

        val loc = LocationStorage.getLocation(requireActivity().applicationContext)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RC_LOCATION) getLocation()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val perms = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ActivityCompat.requestPermissions(requireActivity(), perms, RC_LOCATION)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                Log.d("LOCATION", "Location obtenida del lastLocation: $it")
                updateLocation(it)
            } else {
                Log.d("LOCATION", "No había location guardada, pidiendo location.")
                val mLocationRequest = LocationRequest.create()
                mLocationRequest.interval = 1000
                mLocationRequest.fastestInterval = 500
                mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

                val mLocationCallback: LocationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        for (location in locationResult.locations) {
                            if (location != null) {
                                updateLocation(location)
                                Log.d("LOCATION", "Location obtenida del request manual: $location")
                                fusedLocationClient.removeLocationUpdates(this)
                                return // return para frenar cuando encuentre la primera location no nula
                            }
                        }
                    }
                }

                fusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    mLocationCallback,
                    null
                )
            }
        }

    }

    fun updateLocation(location: Location) {
        updateMap(location)
        savedLocation = location
    }

    private fun updateMap(location: Location) {
        val mapFragment = childFragmentManager.findFragmentByTag("test_map") as SupportMapFragment

        mapFragment.getMapAsync { map ->
            val marker = MarkerOptions()
            val position = LatLng(location.latitude, location.longitude)
            marker.position(position)
            map.addMarker(marker)


            map.moveCamera(CameraUpdateFactory.zoomTo(16.0F))
            map.moveCamera(CameraUpdateFactory.newLatLng(position))
        }
    }

    private fun updateStoredLocation(location: Location) {
        val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        val addressLine = address[0].getAddressLine(0)
        LocationStorage.setLocation(requireActivity().applicationContext, location.latitude, location.longitude, addressLine)
        val location = LocationStorage.getLocation(requireActivity().applicationContext)
        Log.d("LOCATION", location.toString())
    }
}