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

    var userKeyDecrypt: String? = ""
    var userKeyEncrypt: JsonElement? = null

    var secretEncrypt: JsonElement? = null
    var secretDecrypt: String? = "333"


    var sessionKey: String = "" // расшифрованный ключ
    var sessionKeyEncypted: JsonElement? = null
    var user: String = ""

    var openX: Int? = null
    var openN: Int? = null
    var privateP: Int? = null
    var privateQ: Int? = null

    var encryptedPassword: JsonElement? = null

    var encryptedText: JsonElement? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState)
        val contains = mSettings?.contains(saved_n)
//        if (contains != null && contains == true) {
//            setContentView(R.layout.activity_main)
//            openX = mSettings?.getInt(saved_x, 61)
//            openN = mSettings?.getInt(saved_n, 27)
//            privateP = mSettings?.getInt(saved_p, 31)
//            privateQ = mSettings?.getInt(saved_q, 31)
//        } else
        setContentView(R.layout.generate_key)
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
                            privateP = pText.toInt()
                            privateQ = qText.toInt()
                            saveKey(
                                openX?.toInt()!!,
                                openN?.toInt()!!,
                                privateP?.toInt()!!,
                                privateQ?.toInt()!!
                            )
                            setContentView(R.layout.activity_main)
                        } catch (ex: Exception) {
                            Toast.makeText(
                                applicationContext,
                                responseJson["error"].asString,
                                Toast.LENGTH_SHORT
                            ).show()

                        }
                        val jsonObj = JsonObject()
                        val key = JsonObject()
                        key.addProperty("x", openX)
                        key.addProperty("n", openN)
                        jsonObj.add("key", key)
                        getUserKeyEncrypt(jsonObj)

                    }
                }
            })
    }

    fun getUserKeyEncrypt(jsonObj: JsonObject) {

        APIServiceClass
            .service
            .getUserKey(jsonObj)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("getUserKeyEncrypt " + t.message)
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {

                        val jsonResponse =
                            JsonParser().parse(response.body()?.string()).asJsonObject
                        userKeyEncrypt = jsonResponse["data"].asJsonObject["userKey"]
                        println("I get encrypted user key: " + userKeyEncrypt)
                        gmDecryptUserKey(privateP!!, privateQ!!, userKeyEncrypt!!);

                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Failed while getting uset key",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })


    }

    fun gmDecryptUserKey(p: Int, q: Int, userKey: JsonElement) {
        val jsonObj = JsonObject()
        jsonObj.add("data", userKey)
        val key = JsonObject()
        key.addProperty("p", p)
        key.addProperty("q", q)
        jsonObj.add("key", key)


        APIServiceClass
            .service
            .gmDecrypt(jsonObj)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("****************** ---TTTT :: POST Throwable EXCEPTION:: " + t.message)
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val jsonResponse =
                            JsonParser().parse(response.body()?.string()).asJsonObject
                        userKeyDecrypt = jsonResponse["data"].asJsonObject["decrypted"].asString

                        println("****************** I decrypt userKey: " + userKeyDecrypt)
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

    fun loginEncryptPassword(view: View) {
        val login = findViewById<EditText>(R.id.loginView).text.toString();
        val password = findViewById<EditText>(R.id.passwordView).text.toString()

        if (login.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                applicationContext,
                "Please enter login and password!!",
                Toast.LENGTH_SHORT
            ).show();
        } else {
            user = login
            val jsonObj = JsonObject()
            jsonObj.addProperty("key", userKeyDecrypt)
            jsonObj.addProperty("data", password)

            APIServiceClass
                .service
                .aesEncrypt(jsonObj)
                .enqueue(object : Callback<ResponseBody> {
                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        println("****************** Error while encrypt password: " + t.message)
                    }

                    override fun onResponse(
                        call: Call<ResponseBody>,
                        response: Response<ResponseBody>
                    ) {
                        if (response.isSuccessful) {
                            try {
                                val jsonResponse =
                                    JsonParser().parse(response.body()?.string()).asJsonObject
                                // получили зашифрованный пароль
                                encryptedPassword =
                                    jsonResponse["data"].asJsonObject["text"]
                                println("****************** encrypted password : " + encryptedPassword)
                                login()
                            } catch (ex: Exception) {

                            }
                        }
                    }
                })
        }
    }

    fun login() {
        val jsonObj = JsonObject()
        val key = JsonObject()
        key.addProperty("x", openX)
        key.addProperty("n", openN)
        jsonObj.add("key", key)
        jsonObj.addProperty("user", user)
        jsonObj.add("password", encryptedPassword)
        jsonObj.add("userKey", userKeyEncrypt)


        APIServiceClass
            .service
            .login(jsonObj)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("****************** Error while login " + t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        try {
                            val jsonResponse =
                                JsonParser().parse(response.body()?.string()).asJsonObject
                            // получили зашифрованную сессию
                            val encryptedSessionKey =
                                jsonResponse["data"].asJsonObject["sessionKey"]

                            println("****************** I get encryptedSessionKey : " + encryptedSessionKey)
                            sessionKeyEncypted = encryptedSessionKey
                            // в sessionKey записываем расшифрованную сессию
                            gmDecryptSessionKey(privateP!!, privateQ!!, encryptedSessionKey!!)
                            setContentView(R.layout.auth_code)
                        } catch (ex: Exception) {
                            Toast.makeText(
                                applicationContext,
                                "Incorrect login or password",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
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

    fun codeLogin(view: View){
        val jsonObj = JsonObject()

        val code = findViewById<EditText>(R.id.authCode)
        jsonObj.addProperty("user", user)
        jsonObj.addProperty("code", code.text.toString())

        APIServiceClass
            .service
            .codeLogin(jsonObj)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("---TTTT :: POST Throwable EXCEPTION:: " + t.message)
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val responseJson = JsonParser().parse(response.body()?.string()).asJsonObject
                        setContentView(R.layout.get_text_layout)
                    }
                }
            })
    }
    fun gmDecryptSessionKey(p: Int, q: Int, session: JsonElement) {
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
                    println("****************** Exception during decryption sessionKey " + t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val jsonResponse =
                            JsonParser().parse(response.body()?.string()).asJsonObject
                        sessionKey = jsonResponse["data"].asJsonObject["decrypted"].asString
                        println("****************** I decrypt session key : " + sessionKey)

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
                        print("******************** I get encrypt text")
                        decryptText(json)
                    }
                }
            })

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
                        print("****************** I decrypt text !!")
                    }
                }
            })
    }

    fun encryptSecret(view: View) {
        val jsonObj = JsonObject()
        jsonObj.addProperty("key", sessionKey)
        jsonObj.addProperty("data", secretDecrypt)

        APIServiceClass
            .service
            .aesEncrypt(jsonObj)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("****************** Error while encrypt ыускуе: " + t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        try {
                            val jsonResponse =
                                JsonParser().parse(response.body()?.string()).asJsonObject
                            // получили зашифрованный пароль
                            secretEncrypt =
                                jsonResponse["data"].asJsonObject["text"]
                            println("****************** encrypted secret : " + secretEncrypt)
                            encryptText()
                        } catch (ex: Exception) {

                        }
                    }
                }
            })

    }

    fun encryptText() {
        val setTextView = findViewById<EditText>(R.id.textViewForText)
        var text = setTextView.text.toString()
        val jsonObj = JsonObject()
        jsonObj.addProperty("data", text)
        jsonObj.addProperty("key", sessionKey)

        APIServiceClass
            .service
            .aesEncrypt(jsonObj)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("****************** Error while encrypt text : " + t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        try {
                            val jsonResponse =
                                JsonParser().parse(response.body()?.string()).asJsonObject
                            // получили зашифрованный пароль
                            encryptedText =
                                jsonResponse["data"].asJsonObject["text"]
                            println("****************** encrypted text : " + encryptedText)
                            setText()
                        } catch (ex: Exception) {

                        }
                    }
                }
            })


    }

    fun setText() {
        val jsonObj = JsonObject()
        jsonObj.add("secret", secretEncrypt)
        jsonObj.addProperty("user", user)
        jsonObj.add("userKey", userKeyEncrypt)
        jsonObj.add("data", encryptedText)

        APIServiceClass.service.setText(jsonObj).enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                println("****************** Error while setText : " + t.message)
            }

            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    try {
                        val jsonResponse =
                            JsonParser().parse(response.body()?.string()).asJsonObject
                        // получили новый секрет
                        secretEncrypt =
                            jsonResponse["data"].asJsonObject["secret"]
                        println("****************** encrypted secret : " + secretEncrypt)
                        var jsonObject = JsonObject()
                        jsonObject.add("encrypted", secretEncrypt)
                        jsonObject.addProperty("key", sessionKey)
                        Toast.makeText(applicationContext, "OK", Toast.LENGTH_LONG).show()
                        secretNewDecrypt(jsonObject)
                    } catch (ex: Exception) {

                    }
                }
            }
        })
    }

    fun secretNewDecrypt(jsonObj: JsonObject) {
        val textView = findViewById<TextView>(R.id.textViewForText)

        APIServiceClass
            .service
            .decryptText(jsonObj)
            .enqueue(object : Callback<ResponseBody> {
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("---TTTT :: POST Throwable EXCEPTION decrypt new secret:: " + t.message)
                }

                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        val jsonResponse =
                            JsonParser().parse(response.body()?.string()).asJsonObject
                        secretDecrypt = jsonResponse["data"].asJsonObject["text"].asString
                        print("****************** I decrypt new secret !!" + secretDecrypt)
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
