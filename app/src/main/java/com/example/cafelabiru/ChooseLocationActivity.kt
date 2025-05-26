package com.example.cafelabiru

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.cafelabiru.databinding.ActivityChooseLocationBinding

class ChooseLocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChooseLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ChooseLocation", "Activity starting...")

        try {
            enableEdgeToEdge()
            Log.d("ChooseLocation", "EdgeToEdge enabled")

            binding = ActivityChooseLocationBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d("ChooseLocation", "ViewBinding set successfully")

            // Test AutoCompleteTextView
            val locationList = arrayOf("Lokasi1", "Lokasi2", "Lokasi3", "Lokasi4")
            val adapter = android.widget.ArrayAdapter(this, android.R.layout.simple_list_item_1, locationList)
            binding.listOfLocation.setAdapter(adapter)
            Log.d("ChooseLocation", "Adapter set successfully")

            // WindowInsets
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                Log.d("ChooseLocation", "WindowInsets applied")
                insets
            }

        } catch (e: Exception) {
            Log.e("ChooseLocation", "Setup error: ${e.message}", e)
            // Fallback ke layout biasa jika binding error
            setContentView(R.layout.activity_choose_location)
        }
    }
}