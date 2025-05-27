package com.example.cafelabiru.history

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cafelabiru.OrderDetailActivity
import com.example.cafelabiru.OrderStatusActivity
import com.example.cafelabiru.R
import com.example.cafelabiru.repository.FirebaseOrderRepository
import com.google.firebase.auth.FirebaseAuth

class OrderHistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OrderHistoryAdapter
    private lateinit var emptyStateText: TextView
    private val firebaseRepository = FirebaseOrderRepository()
    private val auth = FirebaseAuth.getInstance()
    private var currentUserId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        currentUserId = auth.currentUser?.uid

        setupViews(view)
        setupRecyclerView()
        loadUserOrders()
    }

    private fun setupViews(view: View) {
        recyclerView = view.findViewById(R.id.rvOrderHistory)

        // Add empty state text if not in layout
        emptyStateText = TextView(requireContext()).apply {
            text = "Belum ada riwayat pesanan"
            textSize = 16f
            setTextColor(resources.getColor(android.R.color.darker_gray, null))
            textAlignment = View.TEXT_ALIGNMENT_CENTER
            visibility = View.GONE
        }

        // Add empty state to parent layout if it's LinearLayout
        val parent = view as? android.widget.LinearLayout
        parent?.addView(emptyStateText)
    }

    private fun setupRecyclerView() {
        adapter = OrderHistoryAdapter { order ->
            val intent = Intent(requireContext(), OrderStatusActivity::class.java)
            intent.putExtra("ORDER_ID", order.orderId)
            intent.putExtra("USER_ID",currentUserId)
            startActivity(intent)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }


    private fun loadUserOrders() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            showEmptyState(true)
            return
        }

        firebaseRepository.getUserOrders { orders ->
            activity?.runOnUiThread {
                if (orders.isEmpty()) {
                    showEmptyState(true)
                } else {
                    showEmptyState(false)
                    adapter.submitList(orders)
                }
            }
        }
    }

    private fun showEmptyState(show: Boolean) {
        if (show) {
            recyclerView.visibility = View.GONE
            emptyStateText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when fragment becomes visible
        loadUserOrders()
    }
}