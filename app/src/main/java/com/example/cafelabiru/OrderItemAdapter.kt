package com.example.cafelabiru

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cafelabiru.model.OrderMenuItem

class OrderItemAdapter(private val items: List<OrderMenuItem>) :
    RecyclerView.Adapter<OrderItemAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Tambahkan null safety dan error handling
        val imgFood: ImageView? = view.findViewById(R.id.img_food)
        val tvName: TextView? = view.findViewById(R.id.tv_food_name)
        val tvDate: TextView? = view.findViewById(R.id.tvOrderDate)
        val tvPrice: TextView? = view.findViewById(R.id.tv_harga)

        init {
            // Debug log untuk memastikan semua view ditemukan
            android.util.Log.d("OrderItemAdapter", "imgFood: ${imgFood != null}")
            android.util.Log.d("OrderItemAdapter", "tvName: ${tvName != null}")
            android.util.Log.d("OrderItemAdapter", "tvDate: ${tvDate != null}")
            android.util.Log.d("OrderItemAdapter", "tvPrice: ${tvPrice != null}")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        // Gunakan null safety untuk semua operasi
        holder.tvName?.text = item.menuName ?: "-"
        holder.tvPrice?.text = "Rp ${(item.menuPrice ?: 0) * (item.quantity ?: 1)}"
        holder.tvDate?.text = "Jumlah: ${item.quantity ?: 1}"

        // Load image dengan null safety
        holder.imgFood?.let { imageView ->
            Glide.with(holder.itemView.context)
                .load(item.imageResId)
                .placeholder(R.drawable.nasgor)
                .error(R.drawable.nasgor) // Tambahkan error placeholder
                .into(imageView)
        }
    }

    override fun getItemCount(): Int = items.size
}