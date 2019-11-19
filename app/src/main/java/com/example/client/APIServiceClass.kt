package com.example.client

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


class APIServiceClass {

    interface APIService {

        @Headers("Content-type: application/json")
        @POST("/private/gm/generate")
        fun generateOpenKey(@Body body: JsonObject): Call<ResponseBody>

        @Headers("Content-type: application/json")
        @POST("/login")
        fun login(@Body body: JsonObject): Call<ResponseBody>

        @Headers("Content-type: application/json")
        @POST("/private/gm/decrypt")
        fun gmDecrypt(@Body body: JsonObject): Call<ResponseBody>

        @Headers("Content-type: application/json")
        @POST("/file")
        fun getText(@Body body: JsonObject): Call<ResponseBody>

        @Headers("Content-type: application/json")
        @POST("/private/cfb/decrypt")
        fun decryptText(@Body body: JsonObject): Call<ResponseBody>

        @Headers("Content-type: application/json")
        @POST("/private/userKey")
        fun getUserKey(@Body body: JsonObject): Call<ResponseBody>

        @Headers("Content-type: application/json")
        @POST("/private/cfb/encrypt")
        fun aesEncrypt(@Body body: JsonObject): Call<ResponseBody>

        @Headers("Content-type: application/json")
        @POST("/newFile")
        fun setText(@Body body: JsonObject): Call<ResponseBody>

        @Headers("Content-type: application/json", "Connection: close")
        @POST("/login/code")
        fun codeLogin(@Body body: JsonObject): Call<ResponseBody>
    }

    companion object {
        private val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.43.205:5000")
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()

        var service = retrofit.create(APIService::class.java)
    }
}