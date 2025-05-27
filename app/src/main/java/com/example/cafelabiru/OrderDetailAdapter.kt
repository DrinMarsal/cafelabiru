package com.example.cafelabiru

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cafelabiru.databinding.ItemOrderDetailBinding

class OrderDetailAdapter(
    private var orderItems: List<OrderDetailActivity.OrderDetailItem>,
    private val onQuantityChanged: (String, com.example.cafelabiru.model.FoodModel) -> Unit
) : RecyclerView.Adapter<OrderDetailAdapter.OrderDetailViewHolder>() {

    inner class OrderDetailViewHolder(val binding: ItemOrderDetailBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderDetailViewHolder {
        val binding = ItemOrderDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderDetailViewHolder, position: Int) {
        val item = orderItems[position]
        val food = item.food
        val quantity = item.quantity

        holder.binding.apply {
            tvFoodName.text = food.name
            tvFoodDescription.text = food.description
            tvQuantity.text = quantity.toString()
            tvPrice.text = "Rp%.0f".format(food.price * quantity)

            // Load image menggunakan Glide
            Glide.with(holder.itemView.context)
                .load(food.imageUrl)
                .placeholder(R.drawable.placeholder_food) // placeholder jika ada
                .error(R.drawable.placeholder_food) // error image jika ada
                .into(ivFoodImage)

            // Setup click listeners untuk tombol + dan -
            btnMinus.setOnClickListener {
                onQuantityChanged("remove", food)
            }

            btnPlus.setOnClickListener {
                onQuantityChanged("add", food)
            }

            // Enable/disable minus button berdasarkan quantity
            btnMinus.isEnabled = quantity > 0
            btnMinus.alpha = if (quantity > 0) 1.0f else 0.5f
        }
    }

    override fun getItemCount(): Int = orderItems.size

    fun updateData(newOrderItems: List<OrderDetailActivity.OrderDetailItem>) {
        this.orderItems = newOrderItems
        notifyDataSetChanged()
    }
}