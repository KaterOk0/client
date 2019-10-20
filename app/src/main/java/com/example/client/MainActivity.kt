package com.example.client

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }


    fun login(view: View){
        val login = findViewById<EditText>(R.id.loginView).text.toString();
        val password = findViewById<EditText>(R.id.passwordView).text.toString()
        val jsonObj = JsonObject()
        val key = JsonObject()
        key.addProperty("q", 63)
        key.addProperty("p", 52)
        jsonObj.add("key", key)
        jsonObj.addProperty("user", login)
        jsonObj.addProperty("password", password)

        APIServiceClass
            .service
            .login(jsonObj)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("---TTTT :: POST Throwable EXCEPTION:: " + t.message)
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
//                        val jsonResponse = JsonParser().parse(response.body()?.string()).asJsonObject
                        val msg = response.body()?.string()
                        println("---TTTT :: POST msg from server :: " + msg)
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                        setContentView(R.layout.auth_ok)
                    } else {
                        Toast.makeText(applicationContext,"Incorrect login or password", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }
}
