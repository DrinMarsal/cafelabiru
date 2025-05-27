package com.example.cafelabiru

import android.app.AlertDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.cafelabiru.databinding.ActivityOrderDetailBinding
import com.example.cafelabiru.model.OrderMenuItem
import com.example.cafelabiru.model.OrderModel
import com.example.cafelabiru.repository.FirebaseOrderRepository

class OrderDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailBinding
    private lateinit var adapter: OrderDetailAdapter
    private lateinit var userPreferences: UserPreferences

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

        userPreferences = UserPreferences(this)

        setupUI()
        setupRecyclerView()
        calculateTotals()
        setupOrderButton()
        setupClickListeners()

        OrderManager.addOrderChangeListener(orderChangeListener)
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
        binding.layoutAddress.setOnClickListener {
            showEditAddressDialog()
        }
        binding.layoutPhone.setOnClickListener {
            showEditPhoneDialog()
        }
    }

    private fun showEditAddressDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_address, null)
        val etAddress = dialogView.findViewById<EditText>(R.id.etAddress)
        val etAddressDetail = dialogView.findViewById<EditText>(R.id.etAddressDetail)

        etAddress.setText(userPreferences.getUserAddress())
        etAddressDetail.setText(userPreferences.getUserAddressDetail())

        AlertDialog.Builder(this)
            .setTitle("Edit Alamat Pengiriman")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newAddress = etAddress.text.toString().trim()
                val newDetail = etAddressDetail.text.toString().trim()
                if (newAddress.isNotEmpty() && newDetail.isNotEmpty()) {
                    userPreferences.updateAddress(newAddress, newDetail)
                    binding.tvAddress.text = newAddress
                    binding.tvAddressDetail.text = newDetail
                    Toast.makeText(this, "Alamat diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Alamat tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
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
            if (OrderManager.isEmpty()) {
                Toast.makeText(this, "Keranjang kosong!", Toast.LENGTH_SHORT).show()
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

            val order = OrderModel(
                menuItems = orderItems,
                subTotal = subtotal,
                ppn = ppn,
                deliveryFee = deliveryFee,
                total = total,
                customerAddress = userPreferences.getUserAddress(),
                customerAddressDetail = userPreferences.getUserAddressDetail(),
                customerPhone = userPreferences.getUserPhone()
            )

            val firebaseRepository = FirebaseOrderRepository()
            firebaseRepository.saveOrder(order) { success, orderIdOrError ->
                runOnUiThread {
                    binding.btnOrder.isEnabled = true

                    if (success) {
                        Toast.makeText(
                            this,
                            "Pesanan berhasil! Order ID: $orderIdOrError\nTotal: Rp$total",
                            Toast.LENGTH_LONG
                        ).show()
                        OrderManager.clear()
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

    data class OrderDetailItem(
        val food: com.example.cafelabiru.model.FoodModel,
        val quantity: Int
    )
}
