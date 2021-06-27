package com.example.tulook.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*

data class Turno (
    @SerializedName("id")
    @Expose
    var id: Int = 0,

    @SerializedName("peluqueriaId")
    @Expose
    var peluqueriaId: Int = 0,

    @SerializedName("usuarioId")
    @Expose
    var usuarioId: Int = 0,

    @SerializedName("estado")
    @Expose
    var estado: Int = 0,

    @SerializedName("fecha")
    @Expose
    var fecha: Date = Date(),

    @SerializedName("horario")
    @Expose
    var horario: String = "",

    @SerializedName("duracion")
    @Expose
    var duracion: Int = 0
)