package com.example.cafelabiru

import android.content.Intent
import android.widget.SearchView
import android.content.SharedPreferences
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.io.IOException
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var btnSaveLocation: Button
    private lateinit var btnCancel: Button
    private lateinit var tvLocationInfo: TextView
    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences

    private var selectedLocation: LatLng? = null
    private var selectedAddress: String = ""
    private var currentMode: String = "profile" // default mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Initialize Firebase
        database = FirebaseDatabase.getInstance()

        // Initialize SharedPreferences untuk mendapatkan user yang sedang login
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE)

        // Get mode from intent
        currentMode = intent.getStringExtra("mode") ?: "profile"

        // Initialize views
        btnSaveLocation = findViewById(R.id.btnSaveLocation)
        btnCancel = findViewById(R.id.btnCancel)
        tvLocationInfo = findViewById(R.id.tvLocationInfo)

        // Update button text based on mode
        if (currentMode == "order_preview") {
            btnSaveLocation.text = "Pilih Lokasi Ini"
        } else {
            btnSaveLocation.text = "Simpan Lokasi"
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Set click listeners
        btnSaveLocation.setOnClickListener {
            when (currentMode) {
                "order_preview" -> returnSelectedLocation()
                "profile" -> saveLocationToDatabase()
                "order" -> saveLocationToDatabase()
            }
        }

        btnCancel.setOnClickListener {
            finish() // Kembali ke activity sebelumnya
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set default location (Pekanbaru)
        val defaultLocation = LatLng(0.5071, 101.4478)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))

        // Set map click listener
        mMap.setOnMapClickListener { latLng ->
            selectedLocation = latLng

            // Clear previous markers
            mMap.clear()

            // Add marker at selected location
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Lokasi Dipilih")
            )

            // Get address from coordinates
            getAddressFromLocation(latLng)
        }

        // Enable location button if permission is granted
        try {
            mMap.isMyLocationEnabled = true
        } catch (se: SecurityException) {
            // Handle permission not granted
        }
    }

    private fun getAddressFromLocation(latLng: LatLng) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses: List<Address>? =
                geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                selectedAddress = address.getAddressLine(0)
                tvLocationInfo.text = "Lokasi: $selectedAddress"
            } else {
                selectedAddress = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
                tvLocationInfo.text = selectedAddress
            }
        } catch (e: IOException) {
            selectedAddress = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
            tvLocationInfo.text = selectedAddress
        }
    }

    private fun returnSelectedLocation() {
        if (selectedLocation == null) {
            Toast.makeText(this, "Silakan pilih lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        // Return selected address to previous activity
        val resultIntent = Intent()
        resultIntent.putExtra("selected_address", selectedAddress)
        resultIntent.putExtra("selected_lat", selectedLocation!!.latitude)
        resultIntent.putExtra("selected_lng", selectedLocation!!.longitude)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun saveLocationToDatabase() {
        if (selectedLocation == null) {
            Toast.makeText(this, "Silakan pilih lokasi terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid ?: run {
            Toast.makeText(this, "Error: User tidak ditemukan", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance("https://cafelabiru-default-rtdb.asia-southeast1.firebasedatabase.app")

        if (currentMode == "profile") {
            val userRef = database.getReference("user").child(userId)
            userRef.child("location").setValue(selectedAddress)
                .addOnSuccessListener {
                    Toast.makeText(this, "Alamat profil berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan alamat: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else if (currentMode == "order") {
            val orderId = intent.getStringExtra("orderId")
            if (orderId.isNullOrEmpty()) {
                Toast.makeText(this, "Error: orderId tidak ditemukan", Toast.LENGTH_SHORT).show()
                return
            }

            val orderRef = database.getReference("orders").child(userId).child(orderId)
            orderRef.child("locationOrderDetail").setValue(selectedAddress)
                .addOnSuccessListener {
                    Toast.makeText(this, "Alamat pesanan berhasil disimpan", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal menyimpan alamat: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}