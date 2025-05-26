package com.example.cafelabiru

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.DeliveryActivity.Companion.EXTRA_OUTLET_DISTANCE
import com.example.cafelabiru.DeliveryActivity.Companion.EXTRA_OUTLET_NAME
import com.example.cafelabiru.DeliveryActivity.Companion.REQUEST_CODE_PICK_OUTLET
import com.example.cafelabiru.databinding.ActivityTakeawayBinding
import java.util.Calendar

class TakeawayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTakeawayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakeawayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDelivery.setOnClickListener {
            // Cek apakah DeliveryActivity sudah ada di stack
            val intent = Intent(this, DeliveryActivity::class.java)
            // Gunakan FLAG_CLEAR_TOP untuk kembali ke DeliveryActivity yang sudah ada
            // atau buat baru jika belum ada
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish() // tutup TakeawayActivity ini
        }


        binding.outletCard.setOnClickListener {
            val intent = Intent(this, DineInActivity::class.java)
            startActivityForResult(intent, DeliveryActivity.REQUEST_CODE_PICK_OUTLET)
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
