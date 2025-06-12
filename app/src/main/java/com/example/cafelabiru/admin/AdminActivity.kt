package com.example.cafelabiru.admin

import android.*
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cafelabiru.AddMenuActivity
import com.example.cafelabiru.R
import com.example.cafelabiru.databinding.ActivityAdminBinding
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private lateinit var database: DatabaseReference
    private lateinit var notificationManager: NotificationManagerCompat

    val CHANNEL_ID = "channelId"
    val CHANNEL_NAME = "ChannelName"
    val NOTIFICATION_ID = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(this)

        notificationManager = NotificationManagerCompat.from(this)
        buatNotifikasiChannel()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 100)
            }
        }


        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseDatabase.getInstance().getReference("user").child(currentUserId)
            .get().addOnSuccessListener { snapshot ->
                val role = snapshot.child("roleProfile").getValue(String::class.java)
                Log.d("MainActivity", "ROLE: $role") // Tambahkan ini dulu untuk cek
                if (role == "admin") {
                    listenForNewOrders()
                }
            }

        binding.fabMain.setOnClickListener {
            val intent = Intent(this, AddMenuActivity::class.java)
            startActivity(intent)
        }

        // Set default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_admin, AdminHomeFragment())
            .commit()

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
    private fun listenForNewOrders() {
        database = FirebaseDatabase.getInstance().getReference("orders")

        database.addChildEventListener(object : ChildEventListener {
            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // Dengarkan semua user
                for (child in snapshot.children) {
                    Log.e("MainActivity", "📦 Order baru ditambahkan!")
                    showLocalNotification("Pesanan Baru", "Ada pesanan baru masuk!")
                    break
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })

        Log.d("MainActivity", "📡 Listening for new orders...")
    }

    private fun buatNotifikasiChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notifikasi Pesanan",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifikasi ketika ada pesanan baru"
                enableLights(true)
                lightColor = Color.BLUE
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showLocalNotification(title: String, message: String) {
        Toast.makeText(this, "Notifikasi: $title", Toast.LENGTH_SHORT).show()
        Log.d("MainActivity", "Menampilkan notifikasi: $title - $message")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logolabiru) // pastikan ada drawable ini
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {

            notificationManager.notify(NOTIFICATION_ID, notification)

        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                100 // request code
            )
        }
    }
}
