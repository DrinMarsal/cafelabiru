package com.example.cafelabiru.admin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.AddMenuActivity
import com.example.cafelabiru.R
import com.example.cafelabiru.databinding.ActivityAdminBinding
import com.example.cafelabiru.home.HomeFragment

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_admin, AdminHomeFragment())
            .commit()

        // FAB click
        binding.fab.setOnClickListener {
            // Misalnya: buka form tambah produk
            startActivity(Intent(this, AddMenuActivity::class.java))
        }

        // Bottom Navigation
        binding.navViewAdmin.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.navigation_home_admin -> AdminHomeFragment()
                R.id.navigation_order_admin -> AdminHistoryFragment()
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
