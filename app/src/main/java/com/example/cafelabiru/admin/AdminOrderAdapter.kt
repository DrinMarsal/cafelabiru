package com.example.cafelabiru.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.cafelabiru.R
import com.example.cafelabiru.model.OrderModel
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AdminOrderAdapter(
    private val orders: List<OrderModel>,
    private val userIds: List<String> // Add this to track user IDs
) : RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFoodName: TextView = itemView.findViewById(R.id.tv_food_name)
        val tvDatetime: TextView = itemView.findViewById(R.id.tv_datetime)
        val tvDelivery: TextView = itemView.findViewById(R.id.tv_delivery)
        val btnAccept: Button = itemView.findViewById(R.id.btn_accept)
        val imgFood: ImageView = itemView.findViewById(R.id.img_food)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_order_admin, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val userId = userIds[position]

        holder.tvFoodName.text = "Pesanan #${order.orderId}"
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.tvDatetime.text = dateFormat.format(Date(order.orderDate))
        holder.tvDelivery.text = order.status
        holder.imgFood.setImageResource(R.drawable.nasgor)

        // Cek status untuk set tombol dan behavior-nya
        if (order.status == "pending") {
            holder.btnAccept.text = "Terima"
            holder.btnAccept.isEnabled = true
            holder.btnAccept.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, android.R.color.holo_green_dark)
        } else {
            holder.btnAccept.text = "Dalam Proses"
            holder.btnAccept.isEnabled = false
            holder.btnAccept.backgroundTintList = ContextCompat.getColorStateList(holder.itemView.context, android.R.color.holo_orange_light)
        }

        holder.btnAccept.setOnClickListener {
            val dbRef = FirebaseDatabase.getInstance().getReference("orders")
            dbRef.child(userId).child(order.orderId).child("status").setValue("accepted")
                .addOnSuccessListener {
                    Toast.makeText(holder.itemView.context, "Order accepted", Toast.LENGTH_SHORT).show()
                    // Update tombol setelah diterima
                    holder.btnAccept.text = "Menunggu"
                    holder.btnAccept.isEnabled = false
                }
                .addOnFailureListener { error ->
                    Toast.makeText(holder.itemView.context, "Failed to accept order: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun getItemCount() = orders.size
}