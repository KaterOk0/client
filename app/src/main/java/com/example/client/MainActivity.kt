package com.example.client

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.JsonParser


class MainActivity : AppCompatActivity() {

    val APP_PREFERENCES = "mysettings"
    var saved_x: String? = "saved_x"
    var saved_n: String? = "saved_n"
    var saved_p: String? = "saved_p"
    var saved_q: String? = "saved_q"
    var mSettings: SharedPreferences? = null

    var sessionKey: String = "" // расшифрованный ключ
    var user: String = ""

    var x: Int? = null
    var n: Int? = null
    var p: Int? = null
    var q: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState)
        val contains = mSettings?.contains(saved_n)
        if (contains != null && contains == true) {
            setContentView(R.layout.activity_main)
            x = mSettings?.getInt(saved_x, 61)
            n = mSettings?.getInt(saved_n, 27)
            p = mSettings?.getInt(saved_p, 31)
            q = mSettings?.getInt(saved_q, 31)
        } else
            setContentView(R.layout.generate_key)
    }


    fun gmDecrypt(p: Int, q: Int, session: JsonElement) {
        val jsonObj = JsonObject()
        jsonObj.add("data", session)
        val key = JsonObject()
        key.addProperty("p", p)
        key.addProperty("q", q)
        jsonObj.add("key", key)


        APIServiceClass
            .service
            .gmDecrypt(jsonObj)
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
                        val jsonResponse = JsonParser().parse(response.body()?.string()).asJsonObject
                        sessionKey = jsonResponse["data"].asJsonObject["decrypted"].asString
                        print(sessionKey)
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

    fun login(view: View) {
        val login = findViewById<EditText>(R.id.loginView).text.toString();
        user = login
        val password = findViewById<EditText>(R.id.passwordView).text.toString()
        val jsonObj = JsonObject()
        val key = JsonObject()
        key.addProperty("x", x)
        key.addProperty("n", n)
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

                        val jsonResponse = JsonParser().parse(response.body()?.string()).asJsonObject
                        // получили зашифрованную сессию
                        val encryptedSessionKey = jsonResponse["data"].asJsonObject["sessionKey"]

                        // в sessionKey записываем расшифрованную сессию
                        gmDecrypt(p!!, q!!, encryptedSessionKey!!)
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
        val pText = findViewById<EditText>(R.id.pView).text.toString();
        val qText = findViewById<EditText>(R.id.qView).text.toString()
        if (pText.isEmpty() || qText.isEmpty()) {
            Toast.makeText(applicationContext, "Please enter p and q", Toast.LENGTH_SHORT).show()
        }


        val jsonObj = JsonObject()
        jsonObj.addProperty("p", pText.toInt())
        jsonObj.addProperty("q", qText.toInt())

        APIServiceClass
            .service
            .generateOpenKey(jsonObj)
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
                        val responseJson =
                            JsonParser().parse(response.body()?.string()).asJsonObject

                        x = responseJson["data"].asJsonObject["x"].asInt
                        n = responseJson["data"].asJsonObject["n"].asInt
                        p = responseJson["data"].asJsonObject["p"].asInt
                        q = responseJson["data"].asJsonObject["q"].asInt
                        saveKey(x?.toInt()!!, n?.toInt()!!, p?.toInt()!!, q?.toInt()!!)
                        setContentView(R.layout.activity_main)

                    }
                }
            })
    }


    fun saveKey(x: Int, n: Int, p: Int, q: Int) {
        val editor = mSettings?.edit()
        editor?.putInt(saved_x, x)
        editor?.putInt(saved_n, n)
        editor?.putInt(saved_p, p)
        editor?.putInt(saved_q, q)
        editor?.apply()
    }
}
