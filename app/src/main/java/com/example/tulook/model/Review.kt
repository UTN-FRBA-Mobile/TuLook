package com.example.tulook.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("comentario")
    @Expose
    var comentario: String,

    @SerializedName("calificacion")
    @Expose
    var calificacion: Float = 0f,

    @SerializedName("peluqueriaId")
    @Expose
    var peluqueriaId: Int,

    @SerializedName("usuarioId")
    @Expose
    var usuarioId: String,

    @SerializedName("id")
    @Expose
    var id: Int = 0
)