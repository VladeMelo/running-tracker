package melo.vlademir.seminario

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import melo.vlademir.seminario.databinding.ActivityMainBinding

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("name_weight", Context.MODE_PRIVATE)

        if (checkUser()) {
            val intent = Intent(this, SecondActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.buttonContinue.setOnClickListener {
            val name = binding.editName.text.toString()
            val weight = binding.editWeight.text.toString().toFloatOrNull()

            if (name.isNotBlank() && weight != null) {
                AlertDialog.Builder(this)
                    .setMessage("Preencheu de forma correta?")
                    .setPositiveButton("Sim") { _, _ ->
                        saveUser(name, weight)

                        val intent = Intent(this, SecondActivity::class.java)
                        intent.putExtra("NAME", name)
                        intent.putExtra("WEIGHT", weight)

                        startActivity(intent)
                        finish()
                    }
                    .setNegativeButton("Não", null)
                    .show()
            } else {
                Toast.makeText(this, "Please enter valid name and weight", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUser(): Boolean {
        val name = sharedPreferences.getString("name", null)
        val weight = sharedPreferences.getFloat("weight", -1f)

        return name != null && weight != -1f
    }

    private fun saveUser(name: String, weight: Float) {
        val editor = sharedPreferences.edit()
        editor.putString("name", name)
        editor.putFloat("weight", weight)
        editor.apply()
    }
}
