package com.example.tulook.fileSystem

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

data class LocationData(val lat: Double, val lng: Double, val addr: String)

class LocationStorage {
    companion object {
        private var PREFERENCE_KEY = "TULOOK_LOCATION_PREFERENCE_KEY"

        fun getLocation(context: Context): LocationData? {
            val pref = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)

            val isSet = pref.contains("LAT") && pref.contains("LNG") && pref.contains("ADDR")

            if (!isSet) return null

            val lat = pref.getDouble("LAT", 0.0)
            val lng = pref.getDouble("LNG", 0.0)
            val addr = pref.getString("ADDR", "No se obtuvo la dirección.")

            return LocationData(lat, lng, addr!!)
        }

        fun setLocation(context: Context, lat: Double, lng: Double, addr: String) {
            val pref = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE)

            with(pref.edit()) {
                putDouble("LAT", lat)
                putDouble("LNG", lng)
                putString("ADDR", addr)
                commit()
            }
        }
    }
}

fun SharedPreferences.Editor.putDouble(key: String, double: Double): SharedPreferences.Editor =
    putLong(key, java.lang.Double.doubleToRawLongBits(double))

fun SharedPreferences.getDouble(key: String, default: Double) =
    java.lang.Double.longBitsToDouble(getLong(key, java.lang.Double.doubleToRawLongBits(default)))