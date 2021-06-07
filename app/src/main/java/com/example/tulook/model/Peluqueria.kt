package com.example.tulook.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class Peluqueria(
    @SerializedName("id")
    @Expose
    var id: Int,

    @SerializedName("nombre")
    @Expose
    var nombre: String,

    @SerializedName("direccionId")
    @Expose
    var direccionId: Int,

    @SerializedName("direccion")
    @Expose
    var direccion: String,

    @SerializedName("servicios")
    @Expose
    var servicios: List<String>,

    @SerializedName("horarioApertura")
    @Expose
    var horarioApertura: Date,

    @SerializedName("horarioCierre")
    @Expose
    var horarioCierre: Date
)