package com.example.cafelabiru.admin

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.cafelabiru.R
import com.example.cafelabiru.model.OrderModel
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AdminOrderAdapter(
    private val orders: List<OrderModel>,
    private val userIds: List<String>,
    private val onItemClick: (userId: String, orderId: String) -> Unit
) : RecyclerView.Adapter<AdminOrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvFoodName: TextView = itemView.findViewById(R.id.tv_food_name)
        val tvDatetime: TextView = itemView.findViewById(R.id.tv_datetime)
        val tvDelivery: TextView = itemView.findViewById(R.id.tv_delivery)
        val btnAccept: Button = itemView.findViewById(R.id.btn_accept)
        val imgFood: ImageView = itemView.findViewById(R.id.img_food)
    }

    private fun setOrderImage(order: OrderModel, imageView: ImageView) {
        if (order.menuItems.isNotEmpty()) {
            val firstMenuItem = order.menuItems[0]
            val imageUrl = firstMenuItem.imageResId // asumsinya ini URL

            Glide.with(imageView.context)
                .load(imageUrl)
                .transform(RoundedCorners(24))
                .placeholder(R.drawable.placeholder_food) // gambar sementara saat loading
                .error(R.drawable.nasgor) // fallback jika gagal
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.nasgor)
        }
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

        holder.itemView.setOnClickListener {
            onItemClick(userIds[position], orders[position].orderId)
        }


        // Logic untuk handle click berdasarkan status
        when (order.status) {
            "pending" -> {
                holder.btnAccept.text = "Terima"
                holder.btnAccept.isEnabled = true
                holder.btnAccept.backgroundTintList = ContextCompat.getColorStateList(
                    holder.itemView.context,
                    android.R.color.holo_green_dark
                )

                holder.btnAccept.setOnClickListener {
                    val dbRef = FirebaseDatabase.getInstance().getReference("orders")
                    dbRef.child(userId).child(order.orderId).child("status").setValue("accepted")
                        .addOnSuccessListener {
                            Toast.makeText(holder.itemView.context, "Order diterima", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(holder.itemView.context, "Failed to accept order: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            "accepted" -> {
                holder.btnAccept.text = "Menunggu"
                holder.btnAccept.isEnabled = true
                holder.btnAccept.backgroundTintList = ContextCompat.getColorStateList(
                    holder.itemView.context,
                    android.R.color.holo_orange_light
                )

                holder.btnAccept.setOnClickListener {
                    AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Selesaikan Pesanan")
                        .setMessage("Apakah Anda yakin pesanan ini sudah selesai?")
                        .setPositiveButton("Ya") { _, _ ->
                            val dbRef = FirebaseDatabase.getInstance().getReference("orders")
                            dbRef.child(userId).child(order.orderId).child("status").setValue("completed")
                                .addOnSuccessListener {
                                    Toast.makeText(holder.itemView.context, "Pesanan selesai", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(holder.itemView.context, "Gagal menyelesaikan pesanan: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .setNegativeButton("Tidak") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }

            }
            "completed" -> {
                holder.btnAccept.text = "Hapus"
                holder.btnAccept.isEnabled = true
                holder.btnAccept.backgroundTintList = ContextCompat.getColorStateList(
                    holder.itemView.context,
                    android.R.color.holo_red_dark
                )

                holder.btnAccept.setOnClickListener {
                    // Konfirmasi sebelum menghapus
                    android.app.AlertDialog.Builder(holder.itemView.context)
                        .setTitle("Konfirmasi Hapus")
                        .setMessage("Apakah Anda yakin ingin menghapus pesanan #${order.orderId}?")
                        .setPositiveButton("Ya") { _, _ ->
                            val dbRef = FirebaseDatabase.getInstance().getReference("orders")
                            dbRef.child(userId).child(order.orderId).removeValue()
                                .addOnSuccessListener {
                                    Toast.makeText(holder.itemView.context, "Pesanan dihapus", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { error ->
                                    Toast.makeText(holder.itemView.context, "Gagal menghapus pesanan: ${error.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .setNegativeButton("Batal", null)
                        .show()
                }
            }
        }

        if (order.menuItems.isNotEmpty()) {
            val firstMenuItem = order.menuItems[0]
            val imageUrl = firstMenuItem.imageResId // asumsinya ini URL

            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .transform(RoundedCorners(24))
                .placeholder(R.drawable.placeholder_food) // gambar sementara saat loading
                .error(R.drawable.nasgor) // fallback jika gagal
                .into(holder.imgFood) // atau holder.imageView sesuai dengan nama di layout
        } else {
            holder.imgFood.setImageResource(R.drawable.nasgor) // atau holder.imageView
        }
    }

    override fun getItemCount() = orders.size
}