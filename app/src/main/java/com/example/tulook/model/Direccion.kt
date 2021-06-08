package com.example.tulook.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Direccion(
    @SerializedName("id")
    @Expose
    var id: Int,

    @SerializedName("calle")
    @Expose
    var calle: String,

    @SerializedName("numero")
    @Expose
    var numero: Int,

    @SerializedName("pais")
    @Expose
    var pais: String,

    @SerializedName("provincia")
    @Expose
    var provincia: String,

    @SerializedName("localidad")
    @Expose
    var localidad: String
)