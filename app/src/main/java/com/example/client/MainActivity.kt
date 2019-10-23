package com.example.client

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.EditText
import android.widget.Scroller
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainActivity : AppCompatActivity() {

    val APP_PREFERENCES = "mysettings"
    var saved_x: String? = "saved_x"
    var saved_n: String? = "saved_n"
    var saved_p: String? = "saved_p"
    var saved_q: String? = "saved_q"
    var mSettings: SharedPreferences? = null

    var sessionKey: String = "" // расшифрованный ключ
    var user: String = ""

    var openX: Int? = null
    var openN: Int? = null
    var privateP: Int? = null
    var privateQ: Int? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState)
        val contains = mSettings?.contains(saved_n)
        if (contains != null && contains == true) {
            setContentView(R.layout.activity_main)
            openX = mSettings?.getInt(saved_x, 61)
            openN = mSettings?.getInt(saved_n, 27)
            privateP = mSettings?.getInt(saved_p, 31)
            privateQ = mSettings?.getInt(saved_q, 31)
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
                        val jsonResponse =
                            JsonParser().parse(response.body()?.string()).asJsonObject
                        sessionKey = jsonResponse["data"].asJsonObject["decrypted"].asString

                        print(sessionKey)
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Failed decrypt",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })


    }

    fun login(view: View) {
        val login = findViewById<EditText>(R.id.loginView).text.toString();
        val password = findViewById<EditText>(R.id.passwordView).text.toString()

        if(login.isEmpty() || password.isEmpty()){
            Toast.makeText(applicationContext, "Please enter login and password!!", Toast.LENGTH_SHORT).show();
        }
        else {
            user = login
            val jsonObj = JsonObject()
            val key = JsonObject()
            key.addProperty("x", openX)
            key.addProperty("n", openN)
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
                            val jsonResponse =
                                JsonParser().parse(response.body()?.string()).asJsonObject
                            // получили зашифрованную сессию
                            val encryptedSessionKey =
                                jsonResponse["data"].asJsonObject["sessionKey"]

                            // в sessionKey записываем расшифрованную сессию
                            gmDecrypt(privateP!!, privateQ!!, encryptedSessionKey!!)
                            setContentView(R.layout.get_text_layout)
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
                        try {
                            openX = responseJson["data"].asJsonObject["x"].asInt
                            openN = responseJson["data"].asJsonObject["n"].asInt
                            privateP = responseJson["data"].asJsonObject["p"].asInt
                            privateQ = responseJson["data"].asJsonObject["q"].asInt
                            saveKey(openX?.toInt()!!, openN?.toInt()!!, privateP?.toInt()!!, privateQ?.toInt()!!)
                            setContentView(R.layout.activity_main)
                        } catch (ex: Exception) {
                            Toast.makeText(
                                applicationContext,
                                responseJson["error"].asString,
                                Toast.LENGTH_SHORT
                            ).show()

                        }

                    }
                }
            })
    }

    fun getText(view: View) {
        val textView = findViewById<TextView>(R.id.textViewForText)
        textView.setScroller(Scroller(applicationContext))
        textView.maxLines = 20
        textView.isVerticalScrollBarEnabled = true
        textView.movementMethod = ScrollingMovementMethod()

        val json = JsonObject()

        json.addProperty("user", user)

        APIServiceClass
            .service
            .getText(json)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("---TTTT :: POST Throwable EXCEPTION:: " + t.message)
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val jsonResponse =
                            JsonParser().parse(response.body()?.string()).asJsonObject
                        println(jsonResponse)
                        val json = JsonObject()
                        json.add("encrypted", jsonResponse["data"].asJsonObject["encrypted"])
                        json.addProperty("key", sessionKey)
                        decryptText(json)
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

    fun decryptText(jsonObj: JsonObject) {
        val textView = findViewById<TextView>(R.id.textViewForText)

        APIServiceClass
            .service
            .decryptText(jsonObj)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("---TTTT :: POST Throwable EXCEPTION:: " + t.message)
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val jsonResponse =
                            JsonParser().parse(response.body()?.string()).asJsonObject
                        textView.text = jsonResponse["data"].asJsonObject["text"].asString
                    }
                }
            })
    }
}
