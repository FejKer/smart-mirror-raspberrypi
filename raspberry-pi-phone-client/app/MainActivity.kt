package com.mradawiec.smart_lustro

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.mradawiec.smart_lustro.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val editTextCity = findViewById<EditText>(R.id.editTextCity)
        val editTextTopics = findViewById<EditText>(R.id.editTextTopics)
        val editTextLanguage = findViewById<EditText>(R.id.editTextLanguage)
        val editTextWifiName = findViewById<EditText>(R.id.editTextWifiName)
        val editTextWifiPassword = findViewById<EditText>(R.id.editTextWifiPassword)
        val editTextWeatherApi = findViewById<EditText>(R.id.editTextWeatherApi)
        val editTextNewsApi = findViewById<EditText>(R.id.editTextNewsApi)
        val buttonSave = findViewById<Button>(R.id.buttonSave)

        editTextCity.setTag(0, "location")
        editTextTopics.setTag(0,  "news-categories")
        editTextLanguage.setTag(0, "news-language")
        editTextWifiName.setTag(0, "wifi-ssid")
        editTextWifiPassword.setTag(0, "wifi-password")
        editTextWeatherApi.setTag(0, "weather-api-key")
        editTextNewsApi.setTag(0, "news-api-key")

        buttonSave.setOnClickListener {
            val city = editTextCity.text.toString()
            val topics = editTextTopics.text.toString()
            val selectedLanguage = editTextLanguage.text.toString()
            val wifiName = editTextWifiName.text.toString()
            val wifiPassword = editTextWifiPassword.text.toString()
            val newsApiKey = editTextNewsApi.text.toString()
            val weatherApiKey = editTextWeatherApi.text.toString()

            val list = mutableListOf(city, topics, selectedLanguage, wifiName, wifiPassword, newsApiKey, weatherApiKey)

            list.forEach { _ ->
                val key = editTextCity.getTag(0)
                val value = editTextCity.text.toString()
                val dto = UpdateDto(key.toString(), value)
            }

            // TODO: Zapisz dane lub prześlij je na Raspberry Pi



        }
    }
}

data class UpdateDto(
    val key: String,
    val value: String
)
