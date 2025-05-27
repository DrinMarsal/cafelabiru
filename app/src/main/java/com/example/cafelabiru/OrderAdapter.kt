package com.example.cafelabiru

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cafelabiru.model.OrderModel

class OrderAdapter(private var orders: List<OrderModel>) :
    RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    class OrderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val menuNamesText: TextView = view.findViewById(R.id.tvFoodName)
        val totalPriceText: TextView = view.findViewById(R.id.tvTotalPrice)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_order, parent, false)
        return OrderViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        val menuNames = order.menuItems.joinToString(", ") { it.menuName }
        holder.menuNamesText.text = menuNames
        holder.totalPriceText.text = "Rp${order.total}"
    }

    override fun getItemCount(): Int = orders.size

    fun submitList(newOrders: List<OrderModel>) {
        orders = newOrders
        notifyDataSetChanged()
    }
}
