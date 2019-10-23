package com.example.client

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*


class APIServiceClass {

    interface APIService {
//        @GET("/users/{user}")
//        fun generateOpenKey(@Path("user") user: String): Call<ResponseBody>


        @Headers("Content-type: application/json")
        @POST("/private/gm/generate")
        fun generateOpenKey(@Body body: JsonObject): Call<ResponseBody>

        @Headers("Content-type: application/json")
        @POST("/login")
        fun login(@Body body: JsonObject): Call<ResponseBody>


        @Headers("Content-type: application/json")
        @POST("/private/gm/decrypt")
        fun gmDecrypt(@Body body: JsonObject): Call<ResponseBody>
    }

    companion object {
        private val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.106:5000")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()

        var service = retrofit.create(APIService::class.java)
    }
}