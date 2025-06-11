// Buat file baru: MyApplication.kt
package com.example.cafelabiru

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class Labiru : Application() {
    override fun onCreate() {
        super.onCreate()
        // Force light theme untuk seluruh aplikasi
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}