package com.example.testwa01

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt

//import com.example.testwa01.Models.WeatherResponse
//import com.example.testwa01.Models.WeatherData
//import com.example.testwa01.Models.City

class MainActivity : AppCompatActivity() {

    private lateinit var citySpinner: Spinner
    private lateinit var daysEditText: EditText
    private lateinit var submitButton: Button
    private lateinit var forecastRecyclerView: RecyclerView
    private lateinit var cities: MutableList<City>
    private val apiKey = "33aa634c216259f797f35e862f073b40" // OpenWeather API key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        citySpinner = findViewById(R.id.citySpinner)
        daysEditText = findViewById(R.id.daysEditText)
        submitButton = findViewById(R.id.submitButton)
        forecastRecyclerView = findViewById(R.id.recyclerView)

        // Populate spinner with cities from XML
        val cityAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.cities_array,
            android.R.layout.simple_spinner_item
        )
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        citySpinner.adapter = cityAdapter

        // Create the list of cities
        cities = mutableListOf()
        for (i in 0 until citySpinner.count) {
            val name = citySpinner.getItemAtPosition(i).toString()
            val latitude = getLatitude(i)
            val longitude = getLongitude(i)
            cities.add(City(name, latitude, longitude))
        }

        //set up recycler view
        forecastRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = ForecastAdapter()
        forecastRecyclerView.adapter = adapter

        // Set click listener for submit button
        submitButton.setOnClickListener {
            // Get selected city and number of days
            val selectedCityIndex = citySpinner.selectedItemPosition

            // Check if a city is selected
            if (selectedCityIndex >= 0 && selectedCityIndex < cities.size) {
                val days = daysEditText.text.toString().toInt()

                // Call function to fetch weather forecast based on selected city and number of days
                fetchWeatherForecast(selectedCityIndex, days, adapter)
            } else {
                Toast.makeText(this, "Please select a city", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeatherForecast(selectedCityIndex: Int, days: Int, adapter: ForecastAdapter) {
        val selectedCity = cities[selectedCityIndex]
        val apiUrl = "https://api.openweathermap.org/data/2.5/forecast/daily?lat=${selectedCity.latitude}&lon=${selectedCity.longitude}&cnt=$days&appid=$apiKey&units=metric"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL(apiUrl)
                val urlConnection = url.openConnection() as HttpsURLConnection
                val response = urlConnection.inputStream.bufferedReader().use { it.readText() }

                launch(Dispatchers.Main) {
                    parseWeatherForecast(response, adapter)
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    Toast.makeText(applicationContext, "Error fetching weather data", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseWeatherForecast(response: String, adapter: ForecastAdapter) {
        val weatherResponse = Gson().fromJson(response, WeatherResponse::class.java)
        val formattedData = weatherResponse.list.map { weatherData ->
            val formattedDate = formatDate(weatherData.dt)
            weatherData.copy(formattedDate = formattedDate)
        }
        adapter.setData(formattedData)
    }


    private fun formatDate(timestamp: Long): String {
        val date = Date(timestamp* 1000)
        val sdf = SimpleDateFormat("dd.MM", Locale.getDefault())
        return sdf.format(date)
    }

    private fun getLatitude(index: Int): Double {
        val latitudeArray = resources.getStringArray(R.array.latitude_array)
        return latitudeArray.getOrElse(index) { "0.0" }.toDouble()
    }

    private fun getLongitude(index: Int): Double {
        val longitudeArray = resources.getStringArray(R.array.longitude_array)
        return longitudeArray.getOrElse(index) { "0.0" }.toDouble()
    }


}

data class City(var name: String, var latitude: Double, var longitude: Double)
data class WeatherResponse(
    val city: City,
    val list: List<WeatherData>
)

data class WeatherData(
    val dt: Long,
    val temp: Temperature,
    var formattedDate: String
) {
    val roundedDayTemperature: Int
        get() = temp.day.roundToInt()

    val roundedNightTemperature: Int
        get() = temp.night.roundToInt()
}

data class Temperature(
    val day: Double,
    val min: Double,
    val max: Double,
    val night: Double,
    val eve: Double,
    val morn: Double
)
