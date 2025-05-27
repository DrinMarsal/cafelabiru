package com.example.cafelabiru

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.databinding.ActivityMainBinding
import com.example.cafelabiru.history.OrderHistoryFragment
import com.example.cafelabiru.home.HomeFragment
import com.example.cafelabiru.profile.ProfileFragment
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        // Set default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, HomeFragment())
            .commit()

        // Bottom Navigation
        binding.navView.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navigation_home -> HomeFragment()
                R.id.navigation_myorder -> OrderHistoryFragment() // Replace with your actual fragment
                R.id.navigation_profile -> ProfileFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment, it)
                    .commit()
                true
            } ?: false
        }
    }
}
