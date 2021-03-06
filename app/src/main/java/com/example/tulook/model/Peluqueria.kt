package com.example.tulook.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.text.DateFormat
import java.util.*

data class Peluqueria(
    @SerializedName("id")
    @Expose
    var id: Int = 0,

    @SerializedName("nombre")
    @Expose
    var nombre: String = "",

    @SerializedName("direccionId")
    @Expose
    var direccionId: Int = 0,

    @SerializedName("direccion")
    @Expose
    var direccion: Direccion? = null,

    @SerializedName("servicios")
    @Expose
    var servicios: Array<String> = emptyArray<String>(),

    @SerializedName("horarioApertura")
    @Expose
    var horarioApertura: Date = Date(),


    @SerializedName("horarioCierre")
    @Expose
    var horarioCierre: Date  = Date(),

    @SerializedName("rating")
    @Expose
    var rating: Float = 0f,

    @SerializedName("imagenes")
    @Expose
    var imagenes: List<String> = emptyList(),

    @SerializedName("latitud")
    @Expose
    var lat: Double = 0.0,

    @SerializedName("longitud")
    @Expose
    var lng: Double = 0.0
)