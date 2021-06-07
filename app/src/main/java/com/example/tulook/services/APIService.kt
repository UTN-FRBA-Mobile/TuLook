package com.example.tulook.services

import com.example.tulook.model.Peluqueria
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface APIService {
    @GET("peluquerias")
    fun getPeluquerias(): Call<List<Peluqueria>>

    companion object {

        var BASE_URL = "https://tu-look-api.herokuapp.com/api/"

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