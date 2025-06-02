package com.example.cafelabiru  // sesuaikan dengan package projectmu

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.ImageButton
import androidx.cardview.widget.CardView


class DineInActivity : AppCompatActivity() {

    private lateinit var btnBackDine: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dinein)

        btnBackDine = findViewById(R.id.backButton)

        // Set aksi ketika tombol WhatsApp ditekan

        btnBackDine.setOnClickListener {
            finish()
        }

    }
}

