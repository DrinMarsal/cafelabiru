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
        binding.outletCard.setOnClickListener {
            val intent = Intent(this, DineInActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_PICK_OUTLET)
        }

        binding.btnDelivery.setOnClickListener {
            // Hapus setDeliveryActive() - biarkan XML yang handle
        }

        binding.ivEditAddress.setOnClickListener {
            Toast.makeText(this, "Edit address clicked", Toast.LENGTH_SHORT).show()
        }

        binding.llDeliveryTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                binding.tvDeliveryTime.text = formattedTime
            }, hour, minute, true)

            timePickerDialog.show()
        }

        binding.btnCancel.setOnClickListener {
            Toast.makeText(this, "Cancel clicked", Toast.LENGTH_SHORT).show()
        }

        binding.btnConfirm.setOnClickListener {
            Toast.makeText(this, "Confirm clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_OUTLET && resultCode == RESULT_OK && data != null) {
            val outletName = data.getStringExtra(EXTRA_OUTLET_NAME) ?: "Unknown Outlet"
            val outletDistance = data.getStringExtra(EXTRA_OUTLET_DISTANCE) ?: ""

            // update UI di outletCard (pastikan di layout ada TextView dengan id berikut)
            val tvName = binding.outletCard.findViewById<TextView>(R.id.tvOutletName)
            val tvDistance = binding.outletCard.findViewById<TextView>(R.id.tvOutletDistance)

            tvName.text = outletName
            tvDistance.text = outletDistance
        }
    }
}