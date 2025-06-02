package com.example.cafelabiru

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.databinding.ActivityDeliveryBinding
import java.util.Calendar
import android.widget.Button


class DeliveryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeliveryBinding

    companion object {
        const val REQUEST_CODE_PICK_OUTLET = 100
        const val EXTRA_OUTLET_NAME = "extra_outlet_name"
        const val EXTRA_OUTLET_DISTANCE = "extra_outlet_distance"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val btnTakeaway: Button = findViewById(R.id.btnTakeaway)
        btnTakeaway.setOnClickListener {
            val intent = Intent(this, TakeawayActivity::class.java)
            startActivity(intent)
            finish()
        }

        // klik outletCard buka DineInActivity untuk pilih outlet

        binding.btnDelivery.setOnClickListener {
            // Hapus setDeliveryActive() - biarkan XML yang handle
        }


        binding.btnCancel.setOnClickListener {
            Toast.makeText(this, "Cancel clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfirm.setOnClickListener {
            Toast.makeText(this, "Confirm clicked", Toast.LENGTH_SHORT).show()
        }
    }
}