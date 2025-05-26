package com.example.cafelabiru

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cafelabiru.databinding.ItemMenuBinding
import com.example.cafelabiru.model.FoodModel

class MenuAdapter(private val foodList: List<FoodModel>) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    private var onItemClickListener: ((FoodModel) -> Unit)? = null
    private var onAddClickListener: ((FoodModel) -> Unit)? = null

    inner class MenuViewHolder(val binding: ItemMenuBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val item = foodList[position]
        holder.binding.tvName.text = item.name
        holder.binding.tvDesc.text = item.description
        holder.binding.tvPrice.text = "Rp %.2f".format(item.price)

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.binding.ivImage)

        // Klik di card/root view untuk ke detail (kecuali tombol add)
        holder.binding.root.setOnClickListener {
            onItemClickListener?.invoke(item)
        }

        // Klik tombol add untuk menambah ke pesanan
        holder.binding.btnAdd.setOnClickListener {
            onAddClickListener?.invoke(item)
        }
    }

    override fun getItemCount(): Int = foodList.size

    fun setOnItemClickListener(listener: (FoodModel) -> Unit) {
        onItemClickListener = listener
    }

    fun setOnAddClickListener(listener: (FoodModel) -> Unit) {
        onAddClickListener = listener
    }
}