package com.example.cafelabiru

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cafelabiru.databinding.ActivityOrderDetailBinding
import com.example.cafelabiru.model.OrderMenuItem
import com.example.cafelabiru.model.OrderModel
import com.example.cafelabiru.repository.FirebaseOrderRepository

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var adapter: OrderDetailAdapter
    private lateinit var userPreferences: UserPreferences
    private lateinit var tvLocationOrder: ImageButton

    // Tambahkan variabel untuk menyimpan lokasi yang dipilih
    private var selectedDeliveryLocation: String = ""
    private var currentOrderId: String = ""

    private val orderChangeListener: () -> Unit = {
        runOnUiThread {
            updateRecyclerView()
            calculateTotals()
            updateItemCount()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        OrderManager.currentOrder

        userPreferences = UserPreferences(this)

        binding.tvLocationOrder.setOnClickListener {
            openMapsActivity()
        }

        setupUI()
        setupRecyclerView()
        calculateTotals()
        setupOrderButton()
        setupClickListeners()

        OrderManager.addOrderChangeListener(orderChangeListener)
    }

    private fun openMapsActivity() {
        try {
            val intentOrder = Intent(this, MapsActivity::class.java)
            intentOrder.putExtra("mode", "order_preview") // Ubah mode
            startActivityForResult(intentOrder, REQUEST_CODE_MAPS)
        } catch (e: Exception) {
            Log.e("OrderDetailActivity", "Error opening MapsActivity: ${e.message}")
            Toast.makeText(this, "Error membuka peta", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_MAPS && resultCode == RESULT_OK) {
            selectedDeliveryLocation = data?.getStringExtra("selected_address") ?: ""
            if (selectedDeliveryLocation.isNotEmpty()) {
                // Update tampilan alamat dengan lokasi yang dipilih
                binding.tvAddress.text = "Lokasi Pengiriman"
                binding.tvAddressDetail.text = selectedDeliveryLocation
                Toast.makeText(this, "Lokasi pengiriman berhasil dipilih", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        OrderManager.removeOrderChangeListener(orderChangeListener)
    }

    private fun setupUI() {
        binding.tvAddress.text = userPreferences.getUserAddress()
        binding.tvAddressDetail.text = userPreferences.getUserAddressDetail()
        binding.tvPhone.text = userPreferences.getUserPhone()
        updateItemCount()
    }

    private fun setupClickListeners() {
        binding.layoutPhone.setOnClickListener {
            showEditPhoneDialog()
        }
    }



    private fun showEditPhoneDialog() {
        val etPhone = EditText(this)
        etPhone.setText(userPreferences.getUserPhone())
        etPhone.hint = "Masukkan nomor telepon"

        AlertDialog.Builder(this)
            .setTitle("Edit Nomor Telepon")
            .setView(etPhone)
            .setPositiveButton("Simpan") { _, _ ->
                val newPhone = etPhone.text.toString().trim()
                if (newPhone.isNotEmpty()) {
                    userPreferences.updatePhone(newPhone)
                    binding.tvPhone.text = newPhone
                    Toast.makeText(this, "Nomor telepon diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Nomor telepon tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun setupRecyclerView() {
        val orderList = OrderManager.getOrderMap().map { (food, qty) ->
            OrderDetailItem(food, qty)
        }
        adapter = OrderDetailAdapter(orderList) { action, food ->
            when (action) {
                "add" -> OrderManager.addItem(food)
                "remove" -> OrderManager.removeItem(food)
            }
        }
        binding.recyclerViewOrder.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewOrder.adapter = adapter
        binding.recyclerViewOrder.isNestedScrollingEnabled = false // <- tambahkan ini
    }

    private fun updateRecyclerView() {
        val orderList = OrderManager.getOrderMap().map { (food, qty) ->
            OrderDetailItem(food, qty)
        }
        adapter.updateData(orderList)
    }

    private fun calculateTotals() {
        val subtotal = OrderManager.getTotalPrice()
        val ppn = subtotal * 0.11
        val deliveryFee = 7000.0
        val total = subtotal + ppn + deliveryFee

        binding.tvSubtotal.text = "Rp%.0f".format(subtotal)
        binding.tvPpn.text = "Rp%.0f".format(ppn)
        binding.tvDeliveryFee.text = "Rp%.0f".format(deliveryFee)
        binding.tvTotal.text = "Rp%.0f".format(total)
        binding.tvTotalBottom.text = "Rp%.0f".format(total)
    }

    private fun updateItemCount() {
        val totalItems = OrderManager.getOrderMap().values.sum()
        binding.tvItemCount.text = "$totalItems Item${if (totalItems > 1) "s" else ""}"
    }

    private fun setupOrderButton() {
        binding.btnOrder.setOnClickListener {
            val phone = userPreferences.getUserPhone()
            val address = if (selectedDeliveryLocation.isNotEmpty()) {
                selectedDeliveryLocation
            } else {
                userPreferences.getUserAddress()
            }

            if (OrderManager.isEmpty()) {
                Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.isNullOrEmpty()) {
                Toast.makeText(this, "Silakan isi nomor telepon terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (address.isNullOrEmpty()) {
                Toast.makeText(this, "Silakan pilih lokasi pengiriman terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnOrder.isEnabled = false

            val orderItems = OrderManager.getOrderMap().map { (food, quantity) ->
                OrderMenuItem(
                    menuId = food.menuId,
                    menuName = food.name,
                    menuPrice = food.price.toInt(),
                    quantity = quantity,
                    imageResId = food.imageUrl
                )
            }

            val subtotal = OrderManager.getTotalPrice().toInt()
            val ppn = (subtotal * 0.11).toInt()
            val deliveryFee = 7000
            val total = subtotal + ppn + deliveryFee

            // Gunakan lokasi yang dipilih dari Maps atau alamat default
            val deliveryAddress = if (selectedDeliveryLocation.isNotEmpty()) {
                selectedDeliveryLocation
            } else {
                userPreferences.getUserAddress()
            }

            val addressDetail = if (selectedDeliveryLocation.isNotEmpty()) {
                selectedDeliveryLocation
            } else {
                userPreferences.getUserAddressDetail()
            }

            val order = OrderModel(
                menuItems = orderItems,
                subTotal = subtotal,
                ppn = ppn,
                deliveryFee = deliveryFee,
                total = total,
                customerAddress = deliveryAddress,
                customerAddressDetail = addressDetail,
                customerPhone = userPreferences.getUserPhone()
            )

            val firebaseRepository = FirebaseOrderRepository()
            firebaseRepository.saveOrder(order) { success, orderIdOrError ->
                runOnUiThread {
                    binding.btnOrder.isEnabled = true

                    if (success) {
                        currentOrderId = orderIdOrError

                        // Jika ada lokasi khusus yang dipilih, simpan ke order tersebut
                        if (selectedDeliveryLocation.isNotEmpty()) {
                            saveLocationToOrder(currentOrderId, selectedDeliveryLocation)
                        }

                        Toast.makeText(
                            this,
                            "Pesanan berhasil! Order ID: $orderIdOrError\nTotal: Rp$total",
                            Toast.LENGTH_LONG
                        ).show()
                        OrderManager.clear()
                        val intent = Intent(this, OrderSuccessActivity::class.java)
                        intent.putExtra("ORDER_ID", orderIdOrError)
                        startActivity(intent)
                        finish()

                    } else {
                        Toast.makeText(
                            this,
                            "Gagal menyimpan pesanan: $orderIdOrError",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
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

    data class OrderDetailItem(
        val food: com.example.cafelabiru.model.FoodModel,
        val quantity: Int
    )

    companion object {
        private const val REQUEST_CODE_MAPS = 1001
    }
}