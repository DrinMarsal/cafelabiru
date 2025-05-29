package com.example.cafelabiru

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.model.OrderModel
import com.example.cafelabiru.model.UserModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class OrderStatusActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var tvStatus: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvAddressDet: TextView
    private lateinit var tvNameDet: TextView
    private lateinit var tvNoDet: TextView
    private lateinit var btnOrderDetails: Button

    private var orderAddress: String? = null
    private lateinit var googleMap: GoogleMap
    private var isMapReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_status)

        // Inisialisasi view
        tvStatus = findViewById(R.id.tv_status)
        tvOrderDate = findViewById(R.id.tv_order_date)
        tvAddressDet = findViewById(R.id.tv_address_det)
        tvNameDet = findViewById(R.id.tv_name_det)
        tvNoDet = findViewById(R.id.tv_no_det)
        btnOrderDetails = findViewById(R.id.btnOrderDetails)

        val userId = intent.getStringExtra("USER_ID")
        val orderId = intent.getStringExtra("ORDER_ID")

        if (userId == null || orderId == null) {
            Toast.makeText(this, "Data order tidak lengkap", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Inisialisasi Map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fetchOrderAndUserName(userId, orderId)

        btnOrderDetails.setOnClickListener {
            Toast.makeText(this, "Fitur detail order coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchOrderAndUserName(userId: String, orderId: String) {
        val database = FirebaseDatabase.getInstance()

        val orderRef = database.getReference("orders").child(userId).child(orderId)
        val userRef = database.getReference("user").child(userId)

        orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(orderSnapshot: DataSnapshot) {
                val orderModel = orderSnapshot.getValue(OrderModel::class.java)
                if (orderModel != null) {
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val userModel = userSnapshot.getValue(UserModel::class.java)
                            val userName = userModel?.userName ?: "Nama tidak tersedia"
                            loadOrderStatus(orderModel, userName)
                        }

                        override fun onCancelled(error: DatabaseError) {
                            loadOrderStatus(orderModel, "Nama tidak tersedia")
                        }
                    })
                } else {
                    Toast.makeText(this@OrderStatusActivity, "Order tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OrderStatusActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadOrderStatus(order: OrderModel, userName: String) {
        tvStatus.text = "Order #${order.orderId}\nStatus: ${order.status}"
        val dateFormatted = android.text.format.DateFormat.format("dd MMM yyyy HH:mm", order.orderDate)
        tvOrderDate.text = dateFormatted

        tvAddressDet.text = order.customerAddressDetail
        tvNameDet.text = userName
        tvNoDet.text = order.customerPhone

        orderAddress = order.customerAddressDetail
        updateMapLocation()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true
        updateMapLocation()
    }

    private fun updateMapLocation() {
        if (!::googleMap.isInitialized || !isMapReady || orderAddress.isNullOrEmpty()) return

        try {
            val geocoder = Geocoder(this)
            val addresses = geocoder.getFromLocationName(orderAddress!!, 1)

            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                val latLng = LatLng(location.latitude, location.longitude)

                googleMap.clear()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title("Lokasi Pengantaran")
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            } else {
                Toast.makeText(this, "Gagal menemukan lokasi di peta", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("OrderStatusActivity", "Error geocoding: ${e.message}")
            Toast.makeText(this, "Gagal menampilkan peta", Toast.LENGTH_SHORT).show()
        }
    }
}
