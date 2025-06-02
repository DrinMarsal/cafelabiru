package com.example.cafelabiru

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.DeliveryActivity.Companion.EXTRA_OUTLET_DISTANCE
import com.example.cafelabiru.DeliveryActivity.Companion.EXTRA_OUTLET_NAME
import com.example.cafelabiru.DeliveryActivity.Companion.REQUEST_CODE_PICK_OUTLET
import com.example.cafelabiru.databinding.ActivityTakeawayBinding
import com.example.cafelabiru.model.OrderModel

class TakeawayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTakeawayBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTakeawayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDelivery.setOnClickListener {
            // Reset orderType ke delivery saat pindah ke DeliveryActivity
            OrderManager.setOrderType("delivery")

            // Cek apakah DeliveryActivity sudah ada di stack
            val intent = Intent(this, DeliveryActivity::class.java)
            // Gunakan FLAG_CLEAR_TOP untuk kembali ke DeliveryActivity yang sudah ada
            // atau buat baru jika belum ada
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish() // tutup TakeawayActivity ini
        }


        val btnConfirm = findViewById<Button>(R.id.btn_confirm)
        btnConfirm.setOnClickListener {
            // Set orderType ke "takeaway" di OrderManager
            OrderManager.setOrderType("takeaway")

            // Buat order baru dengan orderType "takeaway"
            val newOrder = OrderModel(orderType = "takeaway")

            // Simpan ke OrderManager
            OrderManager.currentOrder = newOrder

            // Pindah ke MenuActivity
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }



        binding.outletCard.setOnClickListener {
            val intent = Intent(this, DineInActivity::class.java)
            startActivityForResult(intent, DeliveryActivity.REQUEST_CODE_PICK_OUTLET)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_OUTLET && resultCode == RESULT_OK && data != null) {
            val outletName = data.getStringExtra(EXTRA_OUTLET_NAME) ?: "Unknown Outlet"
            val outletDistance = data.getStringExtra(EXTRA_OUTLET_DISTANCE) ?: ""

            // update UI di outletCard (pastikan di layout ada TextView dengan id berikut)
            val tvName = binding.outletCard.findViewById<TextView>(R.id.tvOutletName)


            tvName.text = outletName

        }
    }
}
