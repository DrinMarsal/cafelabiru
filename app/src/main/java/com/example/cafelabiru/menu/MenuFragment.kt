package com.example.cafelabiru

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cafelabiru.databinding.FragmentMenuBinding
import com.example.cafelabiru.model.FoodModel
import com.google.firebase.database.*

class MenuFragment : Fragment() {

    private var _binding: FragmentMenuBinding? = null
    private val binding get() = _binding!!

    private lateinit var database: DatabaseReference
    private lateinit var foodList: ArrayList<FoodModel>
    private lateinit var adapter: MenuAdapter
    private val orderChangeListener = { updateOrderSummary() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = FirebaseDatabase.getInstance("https://cafelabiru-default-rtdb.asia-southeast1.firebasedatabase.app").reference
        foodList = ArrayList()
        adapter = MenuAdapter(foodList)

        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener { food ->
            val intent = Intent(requireContext(), DetailActivity::class.java)
            intent.putExtra("foodItem", food)
            startActivity(intent)
        }

        adapter.setOnAddClickListener { food ->
            OrderManager.addItem(food)
        }

        fetchMenuItems()
        setupOrderButton()

        OrderManager.addOrderChangeListener(orderChangeListener)
        updateOrderSummary()
    }

    override fun onResume() {
        super.onResume()
        updateOrderSummary()
    }

    private fun fetchMenuItems() {
        binding.progressBar.visibility = View.VISIBLE

        val categoryFilter = arguments?.getString("categoryFilter")

        database.child("menu").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                foodList.clear()
                for (dataSnap in snapshot.children) {
                    val item = dataSnap.getValue(FoodModel::class.java)
                    if (item != null && (categoryFilter == null || item.categories.equals(categoryFilter, ignoreCase = true))) {
                        foodList.add(item)
                    }
                }
                adapter.notifyDataSetChanged()
                binding.progressBar.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Gagal memuat data: ${error.message}", Toast.LENGTH_LONG).show()
                binding.progressBar.visibility = View.GONE
            }
        })
    }

    private fun updateOrderSummary() {
        val layoutOrderItems = binding.cardOrderSummary.findViewById<LinearLayout>(R.id.layoutOrderItems)
        layoutOrderItems.removeAllViews()

        val orderMap = OrderManager.getOrderMap()
        val totalPrice = OrderManager.getTotalPrice()
        val inflater = LayoutInflater.from(requireContext())

        for ((food, qty) in orderMap) {
            val itemView = inflater.inflate(R.layout.item_order_summary, layoutOrderItems, false)
            val tvName = itemView.findViewById<TextView>(R.id.tvOrderItemName)
            val tvQty = itemView.findViewById<TextView>(R.id.tvOrderQty)
            val btnPlus = itemView.findViewById<Button>(R.id.btnPlus)
            val btnMinus = itemView.findViewById<Button>(R.id.btnMinus)

            tvName.text = food.name
            tvQty.text = qty.toString()

            btnPlus.setOnClickListener { OrderManager.addItem(food) }
            btnMinus.setOnClickListener { OrderManager.removeItem(food) }

            layoutOrderItems.addView(itemView)
        }

        val tvTotalPrice = binding.cardOrderSummary.findViewById<TextView>(R.id.tvTotalPrice)
        tvTotalPrice.text = "Total: Rp %.2f".format(totalPrice)

        binding.cardOrderSummary.visibility =
            if (OrderManager.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun setupOrderButton() {
        binding.btnOrder.setOnClickListener {
            if (OrderManager.isEmpty()) {
                Toast.makeText(requireContext(), "Pesanan masih kosong", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(requireContext(), OrderDetailActivity::class.java)
                startActivity(intent)
                val pesan = OrderManager.getOrderSummaryText()
                val total = OrderManager.getTotalPrice()
                Toast.makeText(requireContext(), "Pesanan:\n$pesan\nTotal: Rp%.2f".format(total), Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        OrderManager.removeOrderChangeListener(orderChangeListener)
        _binding = null
    }
}
