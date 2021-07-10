package com.example.tulook.services

import com.example.tulook.model.Peluqueria
import com.example.tulook.model.Turno
import com.example.tulook.model.Review
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.*

interface APIService {
    @GET("peluquerias")
    fun getPeluquerias(): Call<List<Peluqueria>>

    @GET("peluquerias/{id}")
    fun getPeluqueria(@Path("id") id: Int): Call<Peluqueria>

    @GET("reviews/byPeluqueria/{id}")
    fun getReviewsPorPeluqueria(@Path("id") id: Int): Call<List<Review>>

    @GET("turnos/byPeluquerias/{peluqueriaId}/{fecha}")
    fun getTurnosPorPeluqueriaPorDia(@Path("peluqueriaId") peluqueriaId: Int, @Path("fecha") fecha: Date): Call<List<Turno>>

    @GET("turnos/byUsuario/{userID}")
    fun getTurnosPorUsuario(@Path("userID") usuarioID: String): Call<List<Turno>>

    @POST("usuarios")
    fun login(@Body data: JsonObject): Call<ResponseBody>

    @POST("reviews")
    fun postNewReview(@Body data: JsonObject): Call<Review>

    companion object {

        var BASE_URL = "https://tu-look-api.herokuapp.com/api/"
//        var BASE_URL = "http://192.168.1.188:3000/api/"

        fun create(): APIService {
            val gson = GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(BASE_URL)
                .build()
            return retrofit.create(APIService::class.java)

        }
    }
}