package com.example.cafelabiru  // sesuaikan dengan package projectmu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import androidx.cardview.widget.CardView


class DineInActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dinein)

        // Misal kamu pasang click listener di CardView pertama dan kedua
        val outlet1 = findViewById<CardView>(R.id.outlet1Card) // nanti tambahkan id di xml
        val outlet2 = findViewById<CardView>(R.id.outlet2Card)
        val outlet3 = findViewById<CardView>(R.id.outlet3Card)

        outlet1.setOnClickListener {
            sendResultAndFinish("Labiru Caffe - Panam", "2.1 km")
        }
        outlet2.setOnClickListener {
            sendResultAndFinish("Labiru Caffe - Gobah", "3.5 km")
        }
        outlet3.setOnClickListener {
            sendResultAndFinish("Labiru Caffe - Senapelan", "4.0 km")
        }
    }

    private fun sendResultAndFinish(name: String, distance: String) {
        val intent = Intent()
        intent.putExtra(DeliveryActivity.EXTRA_OUTLET_NAME, name)
        intent.putExtra(DeliveryActivity.EXTRA_OUTLET_DISTANCE, distance)
        setResult(RESULT_OK, intent)
        finish()
    }
}

