package com.example.tulook.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class Turno(
    @SerializedName("id")
    @Expose
    var id: Int = 0,

    @SerializedName("peluqueriaId")
    @Expose
    var peluqueriaId: Int = 0,

    @SerializedName("usuarioId")
    @Expose
    var usuarioId: String = "0",

    @SerializedName("estado")
    @Expose
    var estado: Int = 0,

    @SerializedName("fecha")
    @Expose
    var fecha: Date = Date(),

    @SerializedName("duracion")
    @Expose
    var duracion: Int = 0,

    @SerializedName("servicios")
    @Expose
    var servicios: Array<String> = emptyArray<String>()
)