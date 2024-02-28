package com.example.testwa01

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.testwa01.WeatherData
import com.example.testwa01.R

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    private val forecastList = mutableListOf<WeatherData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false)
        return ForecastViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val currentForecast = forecastList[position]
        holder.bind(currentForecast)
    }

    override fun getItemCount(): Int {
        return forecastList.size
    }

    fun setData(data: List<WeatherData>) {
        forecastList.clear()
        forecastList.addAll(data)
        notifyDataSetChanged()
    }

    inner class ForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val temperatureTextView: TextView = itemView.findViewById(R.id.TemperatureTextView)
        private val nightTemperatureTextView: TextView = itemView.findViewById(R.id.nightTemperatureTextView)

        fun bind(weatherData: WeatherData) {
            // Bind weather data to views
            dateTextView.text = weatherData.formattedDate // Use the formatted date directly
            temperatureTextView.text = "${weatherData.roundedDayTemperature}°C"
            nightTemperatureTextView.text = "${weatherData.roundedNightTemperature}°C"
        }
    }
}