package com.example.cafelabiru

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cafelabiru.databinding.ActivityDetailBinding
import com.example.cafelabiru.model.FoodModel

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var currentFood: FoodModel? = null
    private val orderChangeListener = { updateOrderSummary() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val food = intent.getSerializableExtra("foodItem") as? FoodModel
        currentFood = food

        if (food != null) {
            binding.tvName.text = food.name
            binding.tvDesc.text = food.description
            binding.tvPrice.text = "Rp %.2f".format(food.price)

            Glide.with(this)
                .load(food.imageUrl)
                .into(binding.ivImage)
        } else {
            binding.tvName.text = "Data tidak tersedia"
        }

        // Setup listeners
        setupButtons()

        // Register for order changes
        OrderManager.addOrderChangeListener(orderChangeListener)

        // Initial update
        updateOrderSummary()
    }

    override fun onDestroy() {
        super.onDestroy()
        OrderManager.removeOrderChangeListener(orderChangeListener)
    }

    private fun setupButtons() {
        // Tombol tambah ke pesanan
        binding.btnAddToOrder.setOnClickListener {
            currentFood?.let { food ->
                OrderManager.addItem(food)
                Toast.makeText(this, "${food.name} ditambahkan ke pesanan", Toast.LENGTH_SHORT).show()
            }
        }

        // Tombol order
        binding.btnOrder.setOnClickListener {
            if (OrderManager.isEmpty()) {
                Toast.makeText(this, "Pesanan masih kosong", Toast.LENGTH_SHORT).show()
            } else {
                val pesan = OrderManager.getOrderSummaryText()
                val total = OrderManager.getTotalPrice()
                Toast.makeText(this, "Pesanan:\n$pesan\nTotal: Rp%.2f".format(total), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun updateOrderSummary() {
        val layoutOrderItems = binding.layoutOrderSummary.findViewById<LinearLayout>(R.id.layoutOrderItems)
        layoutOrderItems.removeAllViews()

        val orderMap = OrderManager.getOrderMap()
        val totalPrice = OrderManager.getTotalPrice()
        val inflater = LayoutInflater.from(this)

        for ((food, qty) in orderMap) {
            val itemView = inflater.inflate(R.layout.item_order_summary, layoutOrderItems, false)
            val tvName = itemView.findViewById<TextView>(R.id.tvOrderItemName)
            val tvQty = itemView.findViewById<TextView>(R.id.tvOrderQty)
            val btnPlus = itemView.findViewById<Button>(R.id.btnPlus)
            val btnMinus = itemView.findViewById<Button>(R.id.btnMinus)

            tvName.text = food.name
            tvQty.text = qty.toString()

            btnPlus.setOnClickListener {
                OrderManager.addItem(food)
            }

            btnMinus.setOnClickListener {
                OrderManager.removeItem(food)
            }

            layoutOrderItems.addView(itemView)
        }

        binding.tvTotalPrice.text = "Total: Rp %.2f".format(totalPrice)

        // Order summary hanya muncul jika ada item, tombol order selalu tampil
        if (OrderManager.isEmpty()) {
            binding.layoutOrderSummary.visibility = android.view.View.GONE
        } else {
            binding.layoutOrderSummary.visibility = android.view.View.VISIBLE
        }
    }
}