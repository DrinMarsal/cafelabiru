package com.example.cafelabiru

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.cafelabiru.databinding.ActivityDetailBinding
import com.example.cafelabiru.model.FoodModel
import com.example.cafelabiru.model.OrderMenuItem
import com.example.cafelabiru.model.OrderModel
import com.example.cafelabiru.repository.FirebaseOrderRepository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var currentFood: FoodModel? = null
    private val orderChangeListener = { updateOrderSummary() }
    private lateinit var userPreferences: UserPreferences
    private lateinit var foodList: ArrayList<FoodModel>
    private lateinit var adapter: MenuAdapter
    private lateinit var database: DatabaseReference

    private var selectedDeliveryLocation: String = ""
    private var currentOrderId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance("https://cafelabiru-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        foodList = ArrayList()
        adapter = MenuAdapter(foodList)

        val food = intent.getSerializableExtra("foodItem") as? FoodModel
        currentFood = food

        userPreferences = UserPreferences(this)

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

        fetchMenuItemById()
        // Setup listeners
        setupButtons()
        setupOrderButton()

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
    }
    private fun saveLocationToOrder(orderId: String, location: String) {
        val firebaseRepository = FirebaseOrderRepository()
        firebaseRepository.updateOrderLocation(orderId, location) { success ->
            if (!success) {
                Log.e("OrderDetailActivity", "Failed to save location to order")
            }
        }
    }

    private fun fetchMenuItemById() {
        val menuId = intent.getStringExtra("menuIdFilter")
        android.util.Log.d("DetailActivity", "Mulai fetch data untuk menuId: $menuId")

        if (menuId == null) {
            return
        }

        database.child("menu").child(menuId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                android.util.Log.d("DetailActivity", "Snapshot diterima: ${snapshot.exists()}")
                if (snapshot.exists()) {
                    val item = snapshot.getValue(FoodModel::class.java)
                    if (item != null) {
                        android.util.Log.d("DetailActivity", "Item berhasil diparsing: ${item.name}")

                        currentFood = item
                        // Tampilkan data di UI DetailActivity
                        binding.tvName.text = item.name
                        binding.tvPrice.text = item.price.toString()
                        binding.tvDesc.text = item.description
                        Glide.with(this@DetailActivity).load(item.imageUrl).into(binding.ivImage)
                    } else {
                        android.util.Log.e("DetailActivity", "Item null untuk menuId: $menuId")
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Data tidak ditemukan untuk menuId: $menuId", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                android.util.Log.e("DetailActivity", "Database error: ${error.message}")
                Toast.makeText(this@DetailActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()
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
            binding.layoutOrderSummary.visibility = android.view.View.GONE
        } else {
            binding.layoutOrderSummary.visibility = android.view.View.VISIBLE
        }
    }

    private fun setupOrderButton() {
        binding.btnOrdery.setOnClickListener {
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