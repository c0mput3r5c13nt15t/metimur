@file:Suppress("DEPRECATION")

package de.c0mput3r5c13nt15t.metimur.ui.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import de.c0mput3r5c13nt15t.metimur.R
import de.c0mput3r5c13nt15t.metimur.databinding.FragmentWeatherBinding
import org.json.JSONObject
import org.json.JSONTokener
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Suppress("DEPRECATION")
class WeatherFragment : Fragment() {

    private var _binding: FragmentWeatherBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    // on below line we are creating variables for our swipe
    // to refresh layout
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val apiKey = "36bfccfe0e1ec11ce99fbfb9a10fecce"

    private fun getCityName(lat: Double,long: Double):String{
        val cityName: String?
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val address = geoCoder.getFromLocation(lat,long,1)
        println(address)
        cityName = address?.get(0)?.locality
        return cityName.toString()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        swipeRefreshLayout = binding.container

        val root: View = binding.root

        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (!isGranted) {
                    val locationView: TextView = binding.location
                    locationView.text = getString(R.string.no_location_access)
                }
            }

        loadWeather(requestPermissionLauncher)

        // on below line we are adding refresh listener
        // for our swipe to refresh method.
        swipeRefreshLayout.setOnRefreshListener {

            println("refresh")

            // Reload the weather
            loadWeather(requestPermissionLauncher)

            // on below line we are setting is refreshing to false.
            swipeRefreshLayout.isRefreshing = false
        }

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadWeather(requestPermissionLauncher: ActivityResultLauncher<String>) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(
                Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        val queue = Volley.newRequestQueue(this.context)
        // Bindings
        val locationView: TextView = binding.location
        // val dateTimeView: TextView = binding.dateTime
        val weatherIconView: ImageView = binding.weatherIcon
        val weatherDescriptionView: TextView = binding.weatherDescription
        val temperatureView: TextView = binding.temperature
        val feelsLikeView: TextView = binding.feelsLike
        val windView: TextView = binding.wind
        val humidityView: TextView = binding.humidity
        val pressureView: TextView = binding.pressure

        val day1WeatherIconView: ImageView = binding.day1WeatherIcon
        val day2WeatherIconView: ImageView = binding.day2WeatherIcon
        val day3WeatherIconView: ImageView = binding.day3WeatherIcon
        val day1WeatherDescriptionView: TextView = binding.day1WeatherDescription
        val day2WeatherDescriptionView: TextView = binding.day2WeatherDescription
        val day3WeatherDescriptionView: TextView = binding.day3WeatherDescription
        val day1TemperatureView: TextView = binding.day1Temperature
        val day2TemperatureView: TextView = binding.day2Temperature
        val day3TemperatureView: TextView = binding.day3Temperature
        val day1WindView: TextView = binding.day1Wind
        val day2WindView: TextView = binding.day2Wind
        val day3WindView: TextView = binding.day3Wind

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude

                    locationView.text = getCityName(lat, lon)

                    val currentWeatherRequestUrl = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=${apiKey}&units=metric"
                    val weatherForecastRequestUrl = "https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&appid=${apiKey}&units=metric"

                    val currentWeatherRequest = StringRequest(
                        Request.Method.GET, currentWeatherRequestUrl,
                        { response ->
                            /*
                            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy '-' HH:mm:ss")
                            val localDateTime: LocalDateTime = LocalDateTime.now()
                            val ldtString = formatter.format(localDateTime)
                            "$ldtString o'clock".also { dateTimeView.text = it } */

                            // Display the first 500 characters of the response string.
                            val jsonObject = JSONTokener(response).nextValue() as JSONObject

                            val firstWeatherJSONObject = jsonObject.getJSONArray("weather").getJSONObject(0)
                            (firstWeatherJSONObject.getString("main")).also { weatherDescriptionView.text = it }
                            val weatherIcon = firstWeatherJSONObject.getString("icon")
                            DownloadImageFromInternet(weatherIconView).execute("https://openweathermap.org/img/wn/${weatherIcon}@2x.png")

                            val mainJSONObject = jsonObject.getJSONObject("main")
                            "${mainJSONObject.getString("temp")}°".also { temperatureView.text = it }
                            "feels like ${mainJSONObject.getString("feels_like")}°".also { feelsLikeView.text = it }
                            "humidity ${mainJSONObject.getString("humidity")}  %".also { humidityView.text = it }
                            "pressure ${mainJSONObject.getString("pressure")} hPa".also { pressureView.text = it }

                            val windJSONObject = jsonObject.getJSONObject("wind")
                            ("wind " + windJSONObject.getString("speed") + " m/s").also { windView.text = it }
                        },
                        { println("That didn't work!") })

                    val weatherForecastRequest = StringRequest(
                        Request.Method.GET, weatherForecastRequestUrl,
                        { response ->

                            val jsonObject = JSONTokener(response).nextValue() as JSONObject

                            val weatherArray = jsonObject.getJSONArray("list")

                            // Weather in 24 Hours

                            val weatherIn24Hours = weatherArray.getJSONObject(5)

                            val firstWeatherIn24JSONObject = weatherIn24Hours.getJSONArray("weather").getJSONObject(0)
                            day1WeatherDescriptionView.text = firstWeatherIn24JSONObject.getString("main")
                            val weatherIn24Icon = firstWeatherIn24JSONObject.getString("icon")
                            DownloadImageFromInternet(day1WeatherIconView).execute("https://openweathermap.org/img/wn/${weatherIn24Icon}@2x.png")

                            val mainIn24JSONObject = weatherIn24Hours.getJSONObject("main")
                            ("Temp: " + mainIn24JSONObject.getString("temp") + " °C").also { day1TemperatureView.text = it }

                            val windIn24JSONObject = weatherIn24Hours.getJSONObject("wind")
                            "Wind: ${windIn24JSONObject.getString("speed")} m/s".also { day1WindView.text = it }

                            // Weather in 48 Hours

                            val weatherIn48Hours = weatherArray.getJSONObject(15)

                            val firstWeatherIn48JSONObject = weatherIn48Hours.getJSONArray("weather").getJSONObject(0)
                            day2WeatherDescriptionView.text = firstWeatherIn48JSONObject.getString("main")
                            val weatherIn48Icon = firstWeatherIn48JSONObject.getString("icon")
                            DownloadImageFromInternet(day2WeatherIconView).execute("https://openweathermap.org/img/wn/${weatherIn48Icon}@2x.png")

                            val mainIn48JSONObject = weatherIn48Hours.getJSONObject("main")
                            ("Temp: " + mainIn48JSONObject.getString("temp") + " °C").also { day2TemperatureView.text = it }

                            val windIn48JSONObject = weatherIn48Hours.getJSONObject("wind")
                            ("Wind: " + windIn48JSONObject.getString("speed") + " m/s").also { day2WindView.text = it }

                            // Weather in 72 Hours

                            val weatherIn72Hours = weatherArray.getJSONObject(23)

                            val firstWeatherIn72JSONObject = weatherIn72Hours.getJSONArray("weather").getJSONObject(0)
                            day3WeatherDescriptionView.text = firstWeatherIn72JSONObject.getString("main")
                            val weatherIn72Icon = firstWeatherIn72JSONObject.getString("icon")
                            DownloadImageFromInternet(day3WeatherIconView).execute("https://openweathermap.org/img/wn/${weatherIn72Icon}@2x.png")

                            val mainIn72JSONObject = weatherIn72Hours.getJSONObject("main")
                            ("Temp: " + mainIn72JSONObject.getString("temp") + " °C").also { day3TemperatureView.text = it }

                            val windIn72JSONObject = weatherIn72Hours.getJSONObject("wind")
                            ("Wind: " + windIn72JSONObject.getString("speed") + " m/s").also { day3WindView.text = it }

                        },
                        { println("That didn't work!") })

                    queue.add(currentWeatherRequest)
                    queue.add(weatherForecastRequest)
                }

            }
    }

    @SuppressLint("StaticFieldLeak")
    @Suppress("DEPRECATION")
    private inner class DownloadImageFromInternet(var imageView: ImageView) : AsyncTask<String, Void, Bitmap?>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg urls: String): Bitmap? {
            val imageURL = urls[0]
            var image: Bitmap? = null
            try {
                val `in` = java.net.URL(imageURL).openStream()
                image = BitmapFactory.decodeStream(`in`)
            }
            catch (e: Exception) {
                println("Error")
                Log.e("Error Message", e.message.toString())
                e.printStackTrace()
            }
            return image
        }
        @Deprecated("Deprecated in Java", ReplaceWith("imageView.setImageBitmap(result)"))
        override fun onPostExecute(result: Bitmap?) {
            imageView.setImageBitmap(result)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}