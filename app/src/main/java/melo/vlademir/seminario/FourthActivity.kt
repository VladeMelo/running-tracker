package melo.vlademir.seminario

import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.content.Intent
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.core.view.setPadding
import melo.vlademir.seminario.databinding.ActivityFourthBinding

class FourthActivity : ComponentActivity() {

    private lateinit var binding: ActivityFourthBinding
    private lateinit var dbHelper: MyDatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences
    private var weight: Float = -1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFourthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbHelper = MyDatabaseHelper(this)

        sharedPreferences = getSharedPreferences("name_weight", Context.MODE_PRIVATE)
        weight = sharedPreferences.getFloat("weight", -1f)

        loadRuns()
    }

    private fun loadRuns() {
        val cursor = dbHelper.getRuns()
        cursor?.use {
            if (it.moveToFirst()) {
                do {
                    val id = it.getLong(it.getColumnIndexOrThrow("_id"))
                    val time = it.getString(it.getColumnIndexOrThrow("time"))
                    val distance = it.getDouble(it.getColumnIndexOrThrow("distance"))

                    addRunToHistory(id, time, distance)
                } while (it.moveToNext())
            }
        }
    }

    private fun addRunToHistory(id: Long, time: String, distance: Double) {
        val runLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24)
            setBackgroundColor(resources.getColor(android.R.color.white))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
            elevation = 4f
        }

        val timeTextView = TextView(this).apply {
            text = time
            textSize = 28f
            setTextColor(resources.getColor(android.R.color.black))
            setPadding(0, 0, 0, 16)
        }

        val horizontalLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val calorieLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        val calorieLabel = TextView(this).apply {
            text = "Calorias"
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            setPadding(0, 0, 0, 4)
        }
        val calorieValue = TextView(this).apply {
            val calorie = if (distance > 0) weight * distance * 1.036 else 0.0
            text = if (distance > 0) String.format("%.0f cal", calorie) else "--"
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black))
        }

        calorieLayout.addView(calorieLabel)
        calorieLayout.addView(calorieValue)

        val distanceLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
        }
        val distanceLabel = TextView(this).apply {
            text = "Distância"
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.darker_gray))
            setPadding(0, 0, 0, 4)
        }
        val distanceValue = TextView(this).apply {
            text = String.format("%.2f km", distance)
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.black))
        }

        distanceLayout.addView(distanceLabel)
        distanceLayout.addView(distanceValue)

        val deleteButton = TextView(this).apply {
            text = "Excluir"
            textSize = 14f
            setTextColor(resources.getColor(android.R.color.holo_red_dark))
            setPadding(16, 0, 16, 0)
            setOnClickListener {
                dbHelper.deleteRun(id)
                binding.historyContainer.removeView(runLayout)
            }
        }

        horizontalLayout.addView(distanceLayout)
        horizontalLayout.addView(calorieLayout)

        horizontalLayout.addView(deleteButton)

        runLayout.addView(timeTextView)
        runLayout.addView(horizontalLayout)

        binding.historyContainer.addView(runLayout, 0)
    }

    override fun onBackPressed() {
        val intent = Intent(this, SecondActivity::class.java)
        startActivity(intent)
        finish()
    }
}
