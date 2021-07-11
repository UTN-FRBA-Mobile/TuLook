package com.example.tulook.ui

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tulook.R
import com.example.tulook.databinding.FragmentPeluqueriasNearMeBinding
import com.example.tulook.fileSystem.MyPreferenceManager
import com.example.tulook.model.Peluqueria
import com.example.tulook.services.APIService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PeluqueriasNearMeFragment : Fragment() {
    private var peluquerias: MutableList<Peluqueria> = mutableListOf()
    private var _binding: FragmentPeluqueriasNearMeBinding? = null
    private val binding get() = _binding!!
    private var hasMovedMap: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeluqueriasNearMeBinding.inflate(inflater, container, false)

        // if the user has saved a location, set location & zoom
        val location = MyPreferenceManager.getLocation(requireContext())
        location?.apply {
            val mapFragment = childFragmentManager.findFragmentByTag("test_map") as SupportMapFragment
            mapFragment.getMapAsync { map ->
                if (!hasMovedMap) {
                    val marker = MarkerOptions()
                    val position = LatLng(lat, lng)
                    marker.position(position)
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_baseline_person_pin_circle))
                    map.addMarker(marker)

                    map.moveCamera(CameraUpdateFactory.zoomTo(16.0F))
                    map.moveCamera(CameraUpdateFactory.newLatLng(position))
                }

                map.setOnCameraMoveListener {
                    hasMovedMap = true
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        getPeluquerias()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun generateMapMarkers() {
        val mapFragment = childFragmentManager.findFragmentByTag("test_map") as SupportMapFragment

        mapFragment.getMapAsync { map ->

            // create markers
            peluquerias.forEach {
                MarkerOptions().apply {
                    Log.d("auth", "${it.lat} ${it.lng}")
                    position(LatLng(it.lat, it.lng))
                    title(it.nombre)

                    map.addMarker(this)?.tag = it.id
                }
            }

            map.setOnMarkerClickListener {
                findNavController().navigate(
                    R.id.peluqueriaDetailFragment,
                    bundleOf("peluqueriaId" to it.tag)
                )

                true
            }
        }

    }

    private fun getPeluquerias() {
        APIService.create().getPeluquerias()
            .enqueue(object : Callback<List<Peluqueria>> {
                override fun onResponse(
                    call: Call<List<Peluqueria>>,
                    response: Response<List<Peluqueria>>
                ) {
                    if (response.isSuccessful) {
                        val body = response.body()
                        if (body != null) {
                            peluquerias.clear()
                            peluquerias.addAll(body)
                            generateMapMarkers()
                        }
                    } else {
                        Toast.makeText(
                            activity,
                            "Ha ocurrido un error obteniendo los datos de las peluquerías",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<Peluqueria>>, t: Throwable) {
                    Log.e(ContentValues.TAG, "onFailure: Ha fallado la llamada")
                }
            })
    }
}
