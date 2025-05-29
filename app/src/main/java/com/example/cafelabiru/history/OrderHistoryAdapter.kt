package com.example.cafelabiru.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cafelabiru.R
import com.example.cafelabiru.model.OrderModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrderHistoryAdapter(
    private val onItemClick: (OrderModel) -> Unit = {}
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder>() {

    private var orders: List<OrderModel> = emptyList()
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    init {
        // Configure currency format for Indonesian Rupiah
        numberFormat.maximumFractionDigits = 0
        numberFormat.currency = java.util.Currency.getInstance("IDR")
    }

    inner class OrderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgFood: ImageView = itemView.findViewById(R.id.img_food)
        val tvMenuNames: TextView = itemView.findViewById(R.id.tv_menu_names)
        val tvTotalPrice: TextView = itemView.findViewById(R.id.tv_harga)
        val tvOrderDate: TextView = itemView.findViewById(R.id.tvOrderDate)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(orders[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_order_history, parent, false)
        return OrderViewHolder(view)
    }

    override fun getItemCount(): Int = orders.size

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]


        // Display menu names (limit to avoid too long text)
        val menuNames = when {
            order.menuItems.isEmpty() -> "Pesanan Kosong"
            order.menuItems.size == 1 -> order.menuItems[0].menuName
            order.menuItems.size <= 3 -> order.menuItems.joinToString(", ") { it.menuName }
            else -> {
                val firstThree = order.menuItems.take(2).joinToString(", ") { it.menuName }
                "$firstThree, +${order.menuItems.size - 2} lainnya"
            }
        }

        holder.tvMenuNames.text = menuNames

        // Format price
        holder.tvTotalPrice.text = formatPrice(order.total)

        // Format date
        val dateString = formatDate(order.orderDate)
        holder.tvOrderDate.text = dateString

        // Set image (use first menu item's image or default)
        if (order.menuItems.isNotEmpty()) {
            val firstMenuItem = order.menuItems[0]
            // If you have image loading logic, implement it here
            // For now, using default image
            holder.imgFood.setImageResource(R.drawable.nasgor) // Default image
        } else {
            holder.imgFood.setImageResource(R.drawable.nasgor) // Default image
        }

        // Add visual indicator based on order status
        val statusColor = when (order.status) {
            "pending" -> R.color.orange
            "accepted" -> R.color.yellow
            "completed" -> R.color.green
            else -> R.color.gray
        }

        // You can add a status indicator view if needed
    }

    private fun formatPrice(price: Int): String {
        return "Rp${String.format("%,d", price).replace(',', '.')}"
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(Date(timestamp))
    }

    fun submitList(newOrders: List<OrderModel>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    fun updateList(newOrders: List<OrderModel>) {
        val oldSize = orders.size
        orders = newOrders

        when {
            oldSize == 0 && newOrders.isNotEmpty() -> {
                notifyItemRangeInserted(0, newOrders.size)
            }
            oldSize > newOrders.size -> {
                notifyItemRangeRemoved(newOrders.size, oldSize - newOrders.size)
                if (newOrders.isNotEmpty()) {
                    notifyItemRangeChanged(0, newOrders.size)
                }
            }
            else -> {
                notifyDataSetChanged()
            }
        }
    }
}