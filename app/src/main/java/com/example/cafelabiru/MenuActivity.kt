package com.example.cafelabiru

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cafelabiru.databinding.ActivityMenuBinding
import com.example.cafelabiru.model.FoodModel
import com.google.firebase.database.*

class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var database: DatabaseReference
    private lateinit var foodList: ArrayList<FoodModel>
    private lateinit var adapter: MenuAdapter
    private val orderChangeListener = { updateOrderSummary() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://cafelabiru-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        foodList = ArrayList()
        adapter = MenuAdapter(foodList)


        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener { food ->
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("foodItem", food)
            startActivity(intent)
        }

        adapter.setOnAddClickListener { food ->
            OrderManager.addItem(food)

        }

        fetchMenuItems()
        setupOrderButton()

        // Register for order changes
        OrderManager.addOrderChangeListener(orderChangeListener)

        // Initial update
        updateOrderSummary()
    }

    override fun onResume() {
        super.onResume()
        // Update order summary when returning from detail activity
        updateOrderSummary()
    }

    private fun fetchMenuItems() {


        val categoryFilter = intent.getStringExtra("categoryFilter")
        android.util.Log.d("MenuActivity", "Mulai fetch data dari Firebase dengan filter kategori: $categoryFilter")

        database.child("menu").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                android.util.Log.d("MenuActivity", "Data snapshot diterima: ${snapshot.exists()}")
                android.util.Log.d("MenuActivity", "Jumlah children: ${snapshot.childrenCount}")

                foodList.clear()
                for (dataSnap in snapshot.children) {
                    android.util.Log.d("MenuActivity", "Processing child: ${dataSnap.key}")
                    val item = dataSnap.getValue(FoodModel::class.java)
                    if (item != null) {
                        android.util.Log.d("MenuActivity", "Item berhasil diparsing: ${item.name}, kategori: ${item.categories}")
                        if (categoryFilter == null || item.categories.equals(categoryFilter, ignoreCase = true)) {
                            foodList.add(item)
                            android.util.Log.d("MenuActivity", "Item ditambahkan ke list: ${item.name}")
                        } else {
                            android.util.Log.d("MenuActivity", "Item dilewati karena tidak cocok filter: ${item.name}")
                        }
                    } else {
                        android.util.Log.e("MenuActivity", "Item null untuk key: ${dataSnap.key}")
                    }
                }

                android.util.Log.d("MenuActivity", "Total items dalam foodList setelah filter: ${foodList.size}")
                adapter.notifyDataSetChanged()

            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("MenuActivity", "Database error: ${error.message}")
                Toast.makeText(this@MenuActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()

            }
        })
    }

    private fun updateOrderSummary() {
        android.util.Log.d("MenuActivity", "===== UPDATE ORDER SUMMARY =====")

        // Akses layoutOrderItems yang ada di dalam cardOrderSummary
        val layoutOrderItems = binding.cardOrderSummary.findViewById<LinearLayout>(R.id.layoutOrderItems)
        layoutOrderItems.removeAllViews()

        val orderMap = OrderManager.getOrderMap()
        val totalPrice = OrderManager.getTotalPrice()
        val inflater = LayoutInflater.from(this)

        android.util.Log.d("MenuActivity", "Order map size: ${orderMap.size}")
        android.util.Log.d("MenuActivity", "Total price: $totalPrice")

        for ((food, qty) in orderMap) {
            android.util.Log.d("MenuActivity", "Menambahkan item ke layout: ${food.name} x $qty")

            val itemView = inflater.inflate(R.layout.item_order_summary, layoutOrderItems, false)
            val tvName = itemView.findViewById<TextView>(R.id.tvOrderItemName)
            val tvQty = itemView.findViewById<TextView>(R.id.tvOrderQty)
            val btnPlus = itemView.findViewById<ImageButton>(R.id.btnPlus)
            val btnMinus = itemView.findViewById<ImageButton>(R.id.btnMinus)

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

        // Update total price - akses tvTotalPrice yang ada di dalam cardOrderSummary
        val tvTotalPrice = binding.cardOrderSummary.findViewById<TextView>(R.id.tvTotalPrice)
        tvTotalPrice.text = "Total: Rp %.2f".format(totalPrice)

        // Order summary visibility - gunakan cardOrderSummary langsung
        if (OrderManager.isEmpty()) {
            android.util.Log.d("MenuActivity", "Order kosong, menyembunyikan summary")
            binding.cardOrderSummary.visibility = View.GONE
        } else {
            android.util.Log.d("MenuActivity", "Order ada, menampilkan summary")
            binding.cardOrderSummary.visibility = View.VISIBLE
        }
    }

    private fun setupOrderButton() {
        binding.btnOrder.setOnClickListener {
            if (OrderManager.isEmpty()) {
                Toast.makeText(this, "Pesanan masih kosong", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, OrderDetailActivity::class.java)
                startActivity(intent)
                val pesan = OrderManager.getOrderSummaryText()
                val total = OrderManager.getTotalPrice()
                Toast.makeText(this, "Pesanan:\n$pesan\nTotal: Rp%.2f".format(total), Toast.LENGTH_LONG).show()
            }
        }
    }
}