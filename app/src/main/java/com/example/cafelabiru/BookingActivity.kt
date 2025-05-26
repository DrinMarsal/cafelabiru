package com.example.cafelabiru

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class BookingActivity : AppCompatActivity() {

    private lateinit var textDescription: TextView
    private lateinit var btnWhatsapp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)  // Pastikan nama layout sesuai file XML

        // Inisialisasi View
        textDescription = findViewById(R.id.text_description)
        btnWhatsapp = findViewById(R.id.btn_whatsapp)

        // Set aksi ketika tombol WhatsApp ditekan
        btnWhatsapp.setOnClickListener {
            val phoneNumber = "6281358213582" // Ganti dengan nomor WhatsApp tujuan, pakai format internasional tanpa "+"
            openWhatsApp(phoneNumber)
        }
    }

    private fun openWhatsApp(phone: String) {
        val url = "https://api.whatsapp.com/send?phone=$phone"
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp tidak terpasang atau error membuka aplikasi", Toast.LENGTH_SHORT).show()
        }
    }
}