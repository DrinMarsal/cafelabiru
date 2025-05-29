package com.example.cafelabiru.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cafelabiru.R
import com.example.cafelabiru.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdminHistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_admin_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvHistory: RecyclerView = view.findViewById(R.id.rv_orders_admin)
        rvHistory.layoutManager = LinearLayoutManager(requireContext())

        val confirmedOrders = mutableListOf<OrderModel>()
        val userIdsList = mutableListOf<String>()

        val ordersRef = FirebaseDatabase.getInstance().getReference("orders")
        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                confirmedOrders.clear()
                userIdsList.clear()

                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue

                    for (orderSnapshot in userSnapshot.children) {
                        val status = orderSnapshot.child("status").getValue(String::class.java)
                        if (status == "accepted") {
                            val order = orderSnapshot.getValue(OrderModel::class.java)
                            if (order != null) {
                                confirmedOrders.add(order)
                                userIdsList.add(userId)
                            }
                        }
                    }
                }

                rvHistory.adapter = AdminOrderAdapter(confirmedOrders, userIdsList)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load history: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
