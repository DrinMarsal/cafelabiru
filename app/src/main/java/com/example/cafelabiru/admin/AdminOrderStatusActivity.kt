package com.example.cafelabiru.admin

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cafelabiru.R
import com.example.cafelabiru.model.OrderMenuItem
import com.example.cafelabiru.model.OrderModel
import com.example.cafelabiru.model.UserModel
import com.example.cafelabiru.OrderItemAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*

class AdminOrderStatusActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var tvStatus: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvAddressDet: TextView
    private lateinit var tvNameDet: TextView
    private lateinit var tvNoDet: TextView
    private lateinit var btnOrderDetails: Button
    private lateinit var recyclerView: RecyclerView // Pastikan ini diinisialisasi

    private lateinit var tvSubtotal: TextView
    private lateinit var tvPpn: TextView
    private lateinit var tvOngkir: TextView
    private lateinit var tvTotal: TextView

    private var orderAddress: String? = null
    private lateinit var googleMap: GoogleMap
    private var isMapReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_order_status)

        // Inisialisasi semua view termasuk RecyclerView
        initViews()

        val userId = intent.getStringExtra("USER_ID")
        val orderId = intent.getStringExtra("ORDER_ID")

        // Debug log untuk memastikan data diterima
        Log.d("OrderStatusActivity", "UserId: $userId, OrderId: $orderId")

        if (userId == null || orderId == null) {
            Toast.makeText(this, "Data order tidak lengkap", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup RecyclerView
        setupRecyclerView()

        // Load menu items
        loadMenuItems(userId, orderId)

        // Inisialisasi Map
        initMap()

        // Fetch order and user data
        fetchOrderAndUserName(userId, orderId)

        btnOrderDetails.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        try {
            tvStatus = findViewById(R.id.tv_status)
            tvOrderDate = findViewById(R.id.tv_order_date)
            tvAddressDet = findViewById(R.id.tv_address_det)
            tvNameDet = findViewById(R.id.tv_name_det)
            tvNoDet = findViewById(R.id.tv_no_det)
            btnOrderDetails = findViewById(R.id.btnOrderDetails)
            recyclerView = findViewById(R.id.recyclerViewOrder) // INI YANG PENTING!

            tvSubtotal = findViewById(R.id.tv_subtotal)
            tvPpn = findViewById(R.id.tv_ppn)
            tvOngkir = findViewById(R.id.tv_ongkir)
            tvTotal = findViewById(R.id.tv_total)

        } catch (e: Exception) {
            Log.e("OrderStatusActivity", "Error initializing views: ${e.message}")
            Toast.makeText(this, "Error loading interface", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Set adapter kosong dulu
        recyclerView.adapter = OrderItemAdapter(emptyList())
    }

    private fun loadMenuItems(userId: String, orderId: String) {
        val databaseRef = FirebaseDatabase.getInstance().getReference("orders/$userId/$orderId/menuItems")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val orderedItems = mutableListOf<OrderMenuItem>()

                    for (itemSnapshot in snapshot.children) {
                        val item = itemSnapshot.getValue(OrderMenuItem::class.java)
                        item?.let {
                            orderedItems.add(it)
                            Log.d("OrderStatusActivity", "Loaded item: ${it.menuName}")
                        }
                    }

                    // Update adapter dengan data baru
                    recyclerView.adapter = OrderItemAdapter(orderedItems)
                    Log.d("OrderStatusActivity", "Total items loaded: ${orderedItems.size}")

                } catch (e: Exception) {
                    Log.e("OrderStatusActivity", "Error processing menu items: ${e.message}")
                    Toast.makeText(this@AdminOrderStatusActivity, "Error loading menu items", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderStatusActivity", "Database error: ${error.message}")
                Toast.makeText(this@AdminOrderStatusActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initMap() {
        try {
            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.mapFragment) as? SupportMapFragment
            mapFragment?.getMapAsync(this)
        } catch (e: Exception) {
            Log.e("OrderStatusActivity", "Error initializing map: ${e.message}")
        }
    }

    private fun fetchOrderAndUserName(userId: String, orderId: String) {
        val database = FirebaseDatabase.getInstance()

        val orderRef = database.getReference("orders").child(userId).child(orderId)
        val userRef = database.getReference("user").child(userId)

        orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(orderSnapshot: DataSnapshot) {
                try {
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
                        Toast.makeText(this@AdminOrderStatusActivity, "Order tidak ditemukan", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                } catch (e: Exception) {
                    Log.e("OrderStatusActivity", "Error processing order data: ${e.message}")
                    Toast.makeText(this@AdminOrderStatusActivity, "Error loading order data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("OrderStatusActivity", "Database error: ${error.message}")
                Toast.makeText(this@AdminOrderStatusActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadOrderStatus(order: OrderModel, userName: String) {
        try {
            tvStatus.text = "Order #${order.orderId}\nStatus: ${order.status}"
            val dateFormatted = android.text.format.DateFormat.format("dd MMM yyyy HH:mm", order.orderDate)
            tvOrderDate.text = dateFormatted

            tvAddressDet.text = order.customerAddressDetail ?: "Alamat tidak tersedia"
            tvNameDet.text = userName
            tvNoDet.text = order.customerPhone ?: "Nomor tidak tersedia"

            orderAddress = order.customerAddressDetail
            tvSubtotal.text = "Rp%.0f".format(order.subTotal.toDouble())
            tvPpn.text = "Rp%.0f".format(order.ppn.toDouble())
            tvOngkir.text = "Rp%.0f".format(order.deliveryFee.toDouble())
            tvTotal.text = "Rp%.0f".format(order.total.toDouble())
            updateMapLocation()
        } catch (e: Exception) {
            Log.e("OrderStatusActivity", "Error loading order status: ${e.message}")
        }
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