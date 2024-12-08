package com.mradawiec.smart_lustro

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private var raspberryPiIp: String? = null
    private var raspberryPiPort: Int = 8080

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val discovery = RaspberryPiDiscovery()
        val ipAddressLabel = findViewById<TextView>(R.id.ipAddressLabel)


        // Używamy coroutines do wywołania funkcji suspend
        CoroutineScope(Dispatchers.IO).launch {
            // Wykrywamy Raspberry Pi dynamicznie w sieci lokalnej
            raspberryPiIp = discovery.test(port = raspberryPiPort)
            withContext(Dispatchers.Main) {
                raspberryPiIp?.let {
                    Log.d("ras", "First Raspberry Pi found at: $it")
                    // Po wykryciu Raspberry Pi, odświeżamy EditText
                    refreshEditTexts()
                    ipAddressLabel.text = it
                } ?: run {
                    Log.d("ras", "No Raspberry Pi found.")
                }
            }
        }

        val editTextCity = findViewById<EditText>(R.id.editTextCity)
        val editTextTopics = findViewById<EditText>(R.id.editTextTopics)
        val editTextLanguage = findViewById<EditText>(R.id.editTextLanguage)
        val editTextWifiName = findViewById<EditText>(R.id.editTextWifiName)
        val editTextWifiPassword = findViewById<EditText>(R.id.editTextWifiPassword)
        val editTextWeatherApi = findViewById<EditText>(R.id.editTextWeatherApi)
        val editTextNewsApi = findViewById<EditText>(R.id.editTextNewsApi)
        val buttonSave = findViewById<Button>(R.id.buttonSave)
        val conntowifi = findViewById<Button>(R.id.conntowifi)


        val editTexts = mapOf(
            "location" to editTextCity,
            "news-categories" to editTextTopics,
            "news-language" to editTextLanguage,
            "wifi-ssid" to editTextWifiName,
            "wifi-password" to editTextWifiPassword,
            "weather-api-key" to editTextWeatherApi,
            "news-api-key" to editTextNewsApi
        )

        val buttonRemoveLocation = findViewById<Button>(R.id.deleteCityButton)
        val buttonRemoveTopics = findViewById<Button>(R.id.deleteTopicsButton)
        val buttonRemoveLanguage = findViewById<Button>(R.id.deleteLanguageButton)
        val buttonRemoveSsid = findViewById<Button>(R.id.deleteSsidButton)
        val buttonRemovePassword = findViewById<Button>(R.id.deletePasswordButton)
        val buttonRemoveWeatherApi = findViewById<Button>(R.id.deleteWeatherApiButton)
        val buttonRemoveNewsApi = findViewById<Button>(R.id.deleteNewsApiButton)
        val buttonsToRemove = listOf(
            buttonRemoveLocation,
            buttonRemoveTopics,
            buttonRemoveLanguage,
            buttonRemoveSsid,
            buttonRemovePassword,
            buttonRemoveWeatherApi,
            buttonRemoveNewsApi
        )

        buttonsToRemove.forEach { button ->
            button.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val key = button.tag.toString()
                    val baseUrl =
                        raspberryPiIp?.let { "http://$it:$raspberryPiPort/api/v1/config/attributes/$key" }
                    val request = baseUrl?.let { it1 ->
                        Request.Builder()
                            .url(it1)
                            .delete()
                            .build()
                    }
                    try {
                        val response = request?.let { it1 -> client.newCall(it1).execute() }
                        withContext(Dispatchers.Main) {
                            if (response != null) {
                                if (response.isSuccessful) {
                                    println("Config updated successfully for key: $key")
                                    editTexts[key]?.setText("")
                                } else {
                                    println("Failed to update config for key: $key, Response: ${response.code}")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            println("Error occurred: ${e.message}")
                        }
                    }
                }
            }

            conntowifi.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val baseUrl =
                        raspberryPiIp?.let { "http://$it:$raspberryPiPort/api/v1/config/wifi-mode" }
                            ?: return@launch

                            val request = Request.Builder()
                                .url(baseUrl)
                                .post("".toRequestBody(null))
                                .build()

                            try {
                                val response = client.newCall(request).execute()
                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {
                                        Toast.makeText(this@MainActivity,"Connected to WiFi", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@MainActivity,"Failed to update config for key", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    println("Error occurred: ${e.message}")
                                }
                            }
                        }
                    }
                }


            // Zapisujemy zaktualizowaną konfigurację
            buttonSave.setOnClickListener {
                CoroutineScope(Dispatchers.IO).launch {
                    val baseUrl =
                        raspberryPiIp?.let { "http://$it:$raspberryPiPort/api/v1/config/attributes" }
                            ?: return@launch

                    editTexts.forEach { (key, editText) ->
                        val value = editText.text.toString()
                        if (value.isNotEmpty()) {
                            val dto = UpdateDto(key, value)
                            val requestBody = Gson().toJson(dto)
                                .toRequestBody("application/json".toMediaTypeOrNull())

                            val request = Request.Builder()
                                .url(baseUrl)
                                .post(requestBody)
                                .build()

                            try {
                                val response = client.newCall(request).execute()
                                withContext(Dispatchers.Main) {
                                    if (response.isSuccessful) {
                                        println("Config updated successfully for key: $key")
                                    } else {
                                        println("Failed to update config for key: $key, Response: ${response.code}")
                                    }
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                withContext(Dispatchers.Main) {
                                    println("Error occurred: ${e.message}")
                                }
                            }
                        }
                    }
                }
            }
        }

    private suspend fun refreshEditTexts() {
        val baseUrl = raspberryPiIp?.let { "http://$it:$raspberryPiPort/api/v1/config/attributes" } ?: return

        Log.d("ras", baseUrl)

        val request = Request.Builder()
            .url(baseUrl)
            .get()
            .build()

        try {
            withContext(Dispatchers.IO) { // Ensure this runs on the IO dispatcher
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        e.printStackTrace()
                        CoroutineScope(Dispatchers.Main).launch {
                            Log.d("ras", "Error fetching data: $e")
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        response.use {
                            if (response.isSuccessful) {
                                val responseData = response.body?.string()
                                Log.d("ras", "Response: $responseData")

                                val configData: Map<String, String> = Gson().fromJson(
                                    responseData,
                                    object : com.google.gson.reflect.TypeToken<Map<String, String>>() {}.type
                                )

                                CoroutineScope(Dispatchers.Main).launch {
                                    val editTexts = mapOf(
                                        "location" to findViewById<EditText>(R.id.editTextCity),
                                        "news-categories" to findViewById<EditText>(R.id.editTextTopics),
                                        "news-language" to findViewById<EditText>(R.id.editTextLanguage),
                                        "wifi-ssid" to findViewById<EditText>(R.id.editTextWifiName),
                                        "wifi-password" to findViewById<EditText>(R.id.editTextWifiPassword),
                                        "weather-api-key" to findViewById<EditText>(R.id.editTextWeatherApi),
                                        "news-api-key" to findViewById<EditText>(R.id.editTextNewsApi)
                                    )

                                    configData.forEach { (key, value) ->
                                        editTexts[key]?.setText(value)
                                    }
                                }
                            } else {
                                CoroutineScope(Dispatchers.Main).launch {
                                    Log.d("ras", "Failed to fetch data: ${response.code}")
                                }
                            }
                        }
                    }
                })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Log.d("ras", "Error fetching data: $e")
            }
        }
    }


    data class UpdateDto(
        val key: String,
        val value: String
    )
}
