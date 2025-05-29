package com.example.cafelabiru.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cafelabiru.R
import com.example.cafelabiru.model.OrderModel
import com.example.cafelabiru.model.OrderMenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminHomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvOrders: RecyclerView = view.findViewById(R.id.rv_orders_admin)
        rvOrders.layoutManager = LinearLayoutManager(requireContext())

        val ordersList = mutableListOf<OrderModel>()
        val userIdsList = mutableListOf<String>()

        val ordersRef = FirebaseDatabase.getInstance().getReference("orders")
        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ordersList.clear()
                userIdsList.clear()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue

                    for (orderSnapshot in userSnapshot.children) {
                        try {
                            val order = orderSnapshot.getValue(OrderModel::class.java)

                            if (order != null && order.status == "pending") {
                                ordersList.add(order)
                                userIdsList.add(userId)
                            }
                        } catch (e: Exception) {
                            val status = orderSnapshot.child("status").getValue(String::class.java)
                            if (status == "pending") {
                                val orderId = orderSnapshot.child("orderId").getValue(String::class.java) ?: ""
                                val total = orderSnapshot.child("total").getValue(Int::class.java) ?: 0
                                val subTotal = orderSnapshot.child("subTotal").getValue(Int::class.java) ?: 0
                                val ppn = orderSnapshot.child("ppn").getValue(Int::class.java) ?: 0
                                val deliveryFee = orderSnapshot.child("deliveryFee").getValue(Int::class.java) ?: 7000
                                val customerAddress = orderSnapshot.child("customerAddress").getValue(String::class.java) ?: ""
                                val customerPhone = orderSnapshot.child("customerPhone").getValue(String::class.java) ?: ""
                                val orderDate = orderSnapshot.child("orderDate").getValue(Long::class.java) ?: System.currentTimeMillis()

                                val menuItems = mutableListOf<OrderMenuItem>()
                                orderSnapshot.child("menuItems").children.forEach { itemSnapshot ->
                                    val item = itemSnapshot.getValue(OrderMenuItem::class.java)
                                    if (item != null) {
                                        menuItems.add(item)
                                    }
                                }

                                val order = OrderModel(
                                    orderId = orderId,
                                    menuItems = menuItems,
                                    subTotal = subTotal,
                                    ppn = ppn,
                                    deliveryFee = deliveryFee,
                                    total = total,
                                    customerAddress = customerAddress,
                                    customerPhone = customerPhone,
                                    orderDate = orderDate,
                                    status = status
                                )

                                ordersList.add(order)
                                userIdsList.add(userId)
                            }
                        }
                    }
                }

                // **PENTING: Beri lambda onItemClick di sini**
                rvOrders.adapter = AdminOrderAdapter(ordersList, userIdsList) { userId, orderId ->
                    Toast.makeText(requireContext(), "Clicked order $orderId of user $userId", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load orders: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
