package com.example.cafelabiru

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.cafelabiru.R
import com.example.cafelabiru.model.OrderModel
import com.example.cafelabiru.model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrderStatusActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvOrderDate: TextView
    private lateinit var tvDelivery: TextView
    private lateinit var tvAddressDet: TextView
    private lateinit var tvNameDet: TextView
    private lateinit var tvNoDet: TextView
    private lateinit var btnOrderDetails: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_status)

        // Inisialisasi view
        tvStatus = findViewById(R.id.tv_status)
        tvOrderDate = findViewById(R.id.tv_order_date)
        tvDelivery = findViewById(R.id.tv_delivery)
        tvAddressDet = findViewById(R.id.tv_address_det)
        tvNameDet = findViewById(R.id.tv_name_det)
        tvNoDet = findViewById(R.id.tv_no_det)
        btnOrderDetails = findViewById(R.id.btnOrderDetails)

        val userId = intent.getStringExtra("USER_ID")
        val orderId = intent.getStringExtra("ORDER_ID")
        val customerName = intent.getStringExtra("CUSTOMER_NAME")


        if (userId == null || orderId == null) {
            Toast.makeText(this, "Data order tidak lengkap", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        fetchOrderAndUserName(userId, orderId)


        btnOrderDetails.setOnClickListener {
            // Aksi tombol
        }
    }

    private fun fetchOrderAndUserName(userId: String, orderId: String) {
        val database = FirebaseDatabase.getInstance()

        // Ref order
        val orderRef = database.getReference("orders").child(userId).child(orderId)
        // Ref user
        val userRef = database.getReference("users").child(userId)

        orderRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(orderSnapshot: DataSnapshot) {
                val orderModel = orderSnapshot.getValue(OrderModel::class.java)
                if (orderModel != null) {
                    // Ambil user name juga
                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnapshot: DataSnapshot) {
                            val userModel = userSnapshot.getValue(UserModel::class.java)
                            val userName = userModel?.userName ?: "Nama tidak tersedia"
                            loadOrderStatus(orderModel, userName)
                        }
                        override fun onCancelled(error: DatabaseError) {
                            // Jika gagal ambil user
                            loadOrderStatus(orderModel, "Nama tidak tersedia")
                        }
                    })
                } else {
                    Toast.makeText(this@OrderStatusActivity, "Order tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@OrderStatusActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }




    private fun loadOrderStatus(order: OrderModel, userName: String) {
        tvStatus.text = "Order #${order.orderId} \nStatus: ${order.status}"

        // Format orderDate dari Long ke tanggal yang bisa dibaca
        val dateFormatted = android.text.format.DateFormat.format("dd MMM yyyy HH:mm", order.orderDate)
        tvOrderDate.text = dateFormatted

        tvDelivery.text = "Delivery Address"
        tvAddressDet.text = order.customerAddressDetail
        tvNameDet.text = userName // Kalau ada nama customer, ambil dari model, kalau gak ada bisa di-hardcode
        tvNoDet.text = order.customerPhone
    }

}
