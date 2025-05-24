package com.example.cafelabiru

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
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
            Toast.makeText(this, "${food.name} ditambahkan ke pesanan", Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        OrderManager.removeOrderChangeListener(orderChangeListener)
    }

    private fun fetchMenuItems() {
        binding.progressBar.visibility = View.VISIBLE

        android.util.Log.d("MenuActivity", "Mulai fetch data dari Firebase")

        database.child("menu").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                android.util.Log.d("MenuActivity", "Data snapshot diterima: ${snapshot.exists()}")
                android.util.Log.d("MenuActivity", "Jumlah children: ${snapshot.childrenCount}")

                foodList.clear()
                for (dataSnap in snapshot.children) {
                    android.util.Log.d("MenuActivity", "Processing child: ${dataSnap.key}")
                    val item = dataSnap.getValue(FoodModel::class.java)
                    if (item != null) {
                        android.util.Log.d("MenuActivity", "Item berhasil diparsing: ${item.name}")
                        foodList.add(item)
                    } else {
                        android.util.Log.e("MenuActivity", "Item null untuk key: ${dataSnap.key}")
                    }
                }

                android.util.Log.d("MenuActivity", "Total items dalam foodList: ${foodList.size}")
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("MenuActivity", "Database error: ${error.message}")
                Toast.makeText(this@MenuActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()
                binding.progressBar.visibility = View.GONE
            }
        })
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
            binding.layoutOrderSummary.visibility = View.GONE
        } else {
            binding.layoutOrderSummary.visibility = View.VISIBLE
        }
    }

    private fun setupOrderButton() {
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
}