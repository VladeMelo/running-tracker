package melo.vlademir.seminario

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import android.util.Log
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import melo.vlademir.seminario.databinding.ActivitySecondBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId

class SecondActivity : ComponentActivity() {

    private lateinit var binding: ActivitySecondBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbHelper: MyDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = MyDatabaseHelper(this)

        sharedPreferences = getSharedPreferences("name_weight", Context.MODE_PRIVATE)

        val name = sharedPreferences.getString("name", null)
        val weight = sharedPreferences.getFloat("weight", -1f)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            getLastLocation()
        }

        binding.welcome.text = "Bem-vindo, $name!"
        binding.weight.text = "Peso: ${weight}kg"

        binding.buttonStart.setOnClickListener {
            val intent = Intent(this, ThirdActivity::class.java)
            startActivity(intent)
        }

        binding.buttonHistory.setOnClickListener {
            val intent = Intent(this, FourthActivity::class.java)
            startActivity(intent)
        }

        updateStatistics()
    }

    private fun updateStatistics() {
        val cursor = dbHelper.getRuns()
        var totalDistance = 0.0
        var runCount = 0

        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val distance = it.getDouble(it.getColumnIndexOrThrow("distance"))
                    totalDistance += distance
                    runCount++
                } while (it.moveToNext())
            }
        }

        binding.totalDistance.text = String.format("%.1f km", totalDistance)
        binding.totalRuns.text = "Total de Corridas: $runCount"
    }

    private fun getLastLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude

                fetchWeatherData(latitude, longitude)
            } else {
                Toast.makeText(this, "Não foi possível obter a localização.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeatherData(latitude: Double, longitude: Double) {
        val client = OkHttpClient()
        val timezone = "America/Sao_Paulo"
        val zoneId = ZoneId.of(timezone)
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude&hourly=temperature_2m,apparent_temperature,precipitation_probability&timezone=$timezone&forecast_days=1"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = Request.Builder()
                    .url(url)
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                if (response.isSuccessful && !responseData.isNullOrEmpty()) {
                    val jsonObject = JSONObject(responseData)
                    val hourly = jsonObject.getJSONObject("hourly")
                    val times = hourly.getJSONArray("time")
                    val temperatures = hourly.getJSONArray("temperature_2m")
                    val apparentTemperatures = hourly.getJSONArray("apparent_temperature")
                    val precipitationProbabilities = hourly.getJSONArray("precipitation_probability")

                    val currentTime = ZonedDateTime.now(zoneId)
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:00")
                    val formattedTime = currentTime.format(formatter)

                    var currentIndex = -1
                    for (i in 0 until times.length()) {
                        if (times.getString(i) == formattedTime) {
                            currentIndex = i
                            break
                        }
                    }

                    if (currentIndex != -1) {
                        val temperature = temperatures.getDouble(currentIndex)
                        val apparentTemperature = apparentTemperatures.getDouble(currentIndex)
                        val precipitationProbability = precipitationProbabilities.getDouble(currentIndex)

                        withContext(Dispatchers.Main) {
                            binding.temperature.text = "Temperatura: ${temperature}°C"
                            binding.apparentTemperature.text = "Sensação Térmica: ${apparentTemperature}°C"
                            binding.precipitationProbability.text = "Chance de Chuva: ${precipitationProbability}%"
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@SecondActivity, "Não foi possível encontrar os dados para a hora atual.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SecondActivity, "Erro ao buscar dados climáticos.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SecondActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
