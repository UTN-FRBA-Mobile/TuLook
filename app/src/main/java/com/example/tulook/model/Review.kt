package com.example.tulook.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Review(
    @SerializedName("id")
    @Expose
    var id: Int,

    @SerializedName("turnoId")
    @Expose
    var turnoId: Int,

    @SerializedName("comentario")
    @Expose
    var comentario: String,

    @SerializedName("calificacion")
    @Expose
    var calificacion: Int
)