package melo.vlademir.seminario

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import melo.vlademir.seminario.databinding.ActivityThirdBinding

class ThirdActivity : ComponentActivity() {

    private lateinit var binding: ActivityThirdBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var dbHelper: MyDatabaseHelper

    private var isRunning = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private var totalDistance = 0.0
    private var lastLocation: Location? = null

    private val simulatedRoute = listOf(
        createLocation(-8.055416600076637, -34.951244173838774),
        createLocation(-8.054900, -34.952000),
        createLocation(-8.053700, -34.953500),
        createLocation(-8.051500, -34.956000),
        createLocation(-8.049053388287438, -34.959934530775314)
    )
    private var currentSimulationIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThirdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = MyDatabaseHelper(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupButtonListeners()
    }

    private fun setupButtonListeners() {
        binding.btnStartStop.setOnClickListener { toggleRun() }
        binding.btnFinish.setOnClickListener { finishRun() }
    }

    private fun toggleRun() {
        if (!isRunning) {
            startRun()
        } else {
            pauseRun()
        }
    }

    private fun startRun() {
        if (checkLocationPermission()) {
            isRunning = true
            startTime = SystemClock.elapsedRealtime() - elapsedTime
            handler.postDelayed(updateTimeTask, 0)
            simulateRun()
            binding.btnStartStop.text = "Parar"
            binding.btnStartStop.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_orange_light))
        } else {
            requestLocationPermission()
        }
    }

    private fun pauseRun() {
        isRunning = false
        elapsedTime = SystemClock.elapsedRealtime() - startTime
        handler.removeCallbacks(updateTimeTask)
        handler.removeCallbacks(simulationTask)
        binding.btnStartStop.text = "Iniciar"
        binding.btnStartStop.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light))
    }

    private fun finishRun() {
        pauseRun()
        val timeString = binding.tvTime.text.toString()
        dbHelper.addRun(timeString, totalDistance)
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
        finish()
    }

    private val updateTimeTask = object : Runnable {
        override fun run() {
            val millis = SystemClock.elapsedRealtime() - startTime
            val seconds = (millis / 1000).toInt()
            val minutes = seconds / 60
            val hours = minutes / 60
            binding.tvTime.text = String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
            handler.postDelayed(this, 1000)
        }
    }

    private val simulationTask = object : Runnable {
        override fun run() {
            if (currentSimulationIndex < simulatedRoute.size) {
                val location = simulatedRoute[currentSimulationIndex]
                updateLocation(location)
                currentSimulationIndex++
                handler.postDelayed(this, 3000)
            } else {
                finishRun()
            }
        }
    }

    private fun simulateRun() {
        currentSimulationIndex = 0
        handler.post(simulationTask)
    }

    private fun updateLocation(location: Location) {
        if (lastLocation != null) {
            val distance = lastLocation!!.distanceTo(location) / 1000 // Convert to km
            totalDistance += distance
            updateDistanceUI()
        }
        lastLocation = location
    }

    private fun updateDistanceUI() {
        binding.tvDistance.text = String.format("%.2f km", totalDistance)
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startRun()
            }
        }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun createLocation(latitude: Double, longitude: Double): Location {
        return Location("gps").apply {
            this.latitude = latitude
            this.longitude = longitude
            this.accuracy = 1f
            this.time = System.currentTimeMillis()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }
}
