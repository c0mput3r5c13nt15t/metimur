package de.c0mput3r5c13nt15t.metimur.ui.weather

import android.annotation.SuppressLint
import de.c0mput3r5c13nt15t.metimur.R

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// on below line we are creating a course rv adapter class.
class WeatherRVAdapter(
    // on below line we are passing variables
    // as course list and context
    private val weatherList: ArrayList<WeatherRVModal>,
    private val context: Context
) : RecyclerView.Adapter<WeatherRVAdapter.WeatherViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): WeatherRVAdapter.WeatherViewHolder {
        // this method is use to inflate the layout file
        // which we have created for our recycler view.
        // on below line we are inflating our layout file.
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.weather_rv_item,
            parent, false
        )
        // at last we are returning our view holder
        // class with our item View File.
        return WeatherViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: WeatherRVAdapter.WeatherViewHolder, position: Int) {
        // on below line we are setting data to our text view and our image view.

        DownloadImageFromInternet(holder.weatherIcon).execute("https://openweathermap.org/img/wn/${weatherList.get(position).weatherIcon}@2x.png")
        holder.weatherDescription.text = weatherList.get(position).weatherDescription
        holder.temperature.text = weatherList.get(position).temperature
        holder.wind.text = weatherList.get(position).wind
    }

    override fun getItemCount(): Int {
        return weatherList.size
    }

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // on below line we are initializing our course name text view and our image view.
        val weatherIcon: ImageView = itemView.findViewById(R.id.idRVWeatherIcon)
        val weatherDescription: TextView = itemView.findViewById(R.id.idRVWeatherDescription)
        val temperature: TextView = itemView.findViewById(R.id.idRVTemperature)
        val wind: TextView = itemView.findViewById(R.id.idRVWind)
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
}
