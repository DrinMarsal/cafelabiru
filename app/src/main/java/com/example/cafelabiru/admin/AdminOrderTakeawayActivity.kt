package com.example.cafelabiru.admin

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cafelabiru.OrderAdapter
import com.example.cafelabiru.R
import com.example.cafelabiru.model.OrderModel
import com.google.firebase.database.*

class AdminOrderTakeawayActivity : AppCompatActivity() {
    private lateinit var databaseRef: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderAdapter
    private val orderList = mutableListOf<OrderModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_order_takeaway)

//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }

        recyclerView = findViewById(R.id.adminOrderTakeaway)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = OrderAdapter(orderList)
        recyclerView.adapter = adapter

        // Firebase
        databaseRef = FirebaseDatabase.getInstance().getReference("order")
        loadOrders()
    }

    private fun loadOrders() {
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                orderList.clear()
                for (data in snapshot.children) {
                    val order = data.getValue(OrderModel::class.java)
                    if (order != null) {
                        orderList.add(order)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", error.message)
            }
        })
    }
}
