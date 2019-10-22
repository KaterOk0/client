package com.example.client

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.JsonParser


class MainActivity : AppCompatActivity() {

    val APP_PREFERENCES = "mysettings"
    var saved_e: String? = "saved_n"
    var saved_n: String? = "saved_e"
    var saved_d: String? = "saved_d"
    var mSettings: SharedPreferences? = null

    var e: Int? = null
    var n: Int? = null
    var d: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState)
        val contains = mSettings?.contains(saved_n)
        if (contains != null && contains == true) {
            setContentView(R.layout.activity_main)
            e = mSettings?.getInt(saved_e, 61)
            n = mSettings?.getInt(saved_n, 27)
            d = mSettings?.getInt(saved_d, 31)
        } else
            setContentView(R.layout.generate_key)
    }


    fun login(view: View) {
        val login = findViewById<EditText>(R.id.loginView).text.toString();
        val password = findViewById<EditText>(R.id.passwordView).text.toString()
        val jsonObj = JsonObject()
        val key = JsonObject()
        key.addProperty("q", e)
        key.addProperty("p", n)
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

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val msg = response.body()?.string()
                        println("---TTTT :: POST msg from server :: " + msg)
                        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                        setContentView(R.layout.auth_ok)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Incorrect login or password",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }

    fun generate_open_key(view: View) {
        val p = findViewById<EditText>(R.id.pView).text.toString();
        val q = findViewById<EditText>(R.id.qView).text.toString()
        if (p.isEmpty() || q.isEmpty() ) {
            Toast.makeText(applicationContext, "Please enter p and q", Toast.LENGTH_SHORT).show()
        }


        val jsonObj = JsonObject()
        jsonObj.addProperty("p", p.toInt())
        jsonObj.addProperty("q", q.toInt())

        APIServiceClass
            .service
            .generateOpenKey(jsonObj)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("---TTTT :: POST Throwable EXCEPTION:: " + t.message)
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseJson = JsonParser().parse(response.body()?.string()).asJsonObject

                        e = responseJson["data"].asJsonObject["e"].asInt
                        n = responseJson["data"].asJsonObject["n"].asInt
                        d = responseJson["data"].asJsonObject["d"].asInt
                        saveKey(e?.toInt()!!, n?.toInt()!!, d?.toInt()!!)
                        setContentView(R.layout.activity_main)

                    }
                }
            })
    }


    fun saveKey(e: Int, n: Int, d: Int) {
        val editor = mSettings?.edit()
        editor?.putInt(saved_e, e)
        editor?.putInt(saved_n, n)
        editor?.putInt(saved_d, d)
        editor?.apply()
    }
}
