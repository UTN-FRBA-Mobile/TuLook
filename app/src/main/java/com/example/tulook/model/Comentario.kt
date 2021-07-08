package com.example.tulook.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class Comentario(
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
    var usuarioId: Int,

    @SerializedName("id")
    @Expose
    var id: Int
)