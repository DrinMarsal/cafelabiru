package com.example.cafelabiru.repository

import android.util.Log
import android.widget.Toast
import com.example.cafelabiru.model.OrderModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FirebaseOrderRepository {

    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("orders")
    private val auth = FirebaseAuth.getInstance()

    fun generateOrderId(): String {
        val randomDigits = (1000..9999).random() // 4 digit angka acak
        return "ORD$randomDigits"
    }

    fun saveOrder(order: OrderModel, callback: (Boolean, String) -> Unit) {
        // Get current user ID
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false, "User not logged in")
            return
        }

        // Generate unique order ID
        var orderId = generateOrderId();
        if (orderId == null) {
            callback(false, "Failed to generate order ID")
            return
        }

        // Create order with proper structure
        val orderWithId = order.copy(
            orderId = orderId
        )

        // Save to Firebase under user's orders
        ordersRef.child(userId).child(orderId).setValue(orderWithId)
            .addOnSuccessListener {
                callback(true, orderId)
            }
            .addOnFailureListener { exception ->
                callback(false, exception.message ?: "Unknown error")
            }
    }


    fun updateOrderLocation(orderId: String, location: String, callback: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        if (userId == null) {
            callback(false)
            return
        }

        val database = FirebaseDatabase.getInstance("https://cafelabiru-default-rtdb.asia-southeast1.firebasedatabase.app")
        val orderRef = database.getReference("orders").child(userId).child(orderId)

        orderRef.child("locationOrderDetail").setValue(location)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseOrderRepository", "Failed to update order location: ${e.message}")
                callback(false)
            }
    }

    fun getUserOrders(userId: String? = null, callback: (List<OrderModel>) -> Unit) {
        // Use current user if userId not provided
        val targetUserId = userId ?: auth.currentUser?.uid

        if (targetUserId == null) {
            callback(emptyList())
            return
        }

        ordersRef.child(targetUserId)
            .orderByChild("orderDate")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val orders = mutableListOf<OrderModel>()

                    for (orderSnapshot in snapshot.children) {
                        try {
                            val order = orderSnapshot.getValue(OrderModel::class.java)
                            order?.let { orders.add(it) }
                        } catch (e: Exception) {
                            // Log error but continue processing other orders
                            e.printStackTrace()
                        }
                    }

                    // Sort by date descending (newest first)
                    orders.sortByDescending { it.orderDate }
                    callback(orders)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(emptyList())
                }
            })
    }

    fun getOrderById(orderId: String, callback: (OrderModel?) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(null)
            return
        }

        ordersRef.child(userId).child(orderId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val order = snapshot.getValue(OrderModel::class.java)
                    callback(order)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }

    fun updateOrderStatus(orderId: String, newStatus: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            callback(false)
            return
        }

        ordersRef.child(userId).child(orderId).child("status").setValue(newStatus)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }
}