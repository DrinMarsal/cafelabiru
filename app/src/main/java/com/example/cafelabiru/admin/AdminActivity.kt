package com.example.cafelabiru.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.R
import com.example.cafelabiru.databinding.ActivityMainBinding
import com.example.cafelabiru.home.HomeFragment

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_admin, HomeFragment())
            .commit()

        // Bottom Navigation
        binding.navView.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navigation_home_admin -> AdminHomeFragment()
                R.id.navigation_order_admin -> AdminHomeFragment()
                R.id.navigation_profile_admin -> AdminProfileFragment()
                else -> null
            }

            fragment?.let {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_admin, it)
                    .commit()
                true
            } ?: false
        }
    }
}
