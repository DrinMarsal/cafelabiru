package com.example.cafelabiru

import com.example.cafelabiru.model.FoodModel
import com.example.cafelabiru.model.OrderModel

object OrderManager {
    private val orderMap = mutableMapOf<FoodModel, Int>()
    private val listeners = mutableListOf<() -> Unit>()

    var currentOrder: OrderModel? = null
    var currentOrderType: String = "delivery"

    fun addItem(food: FoodModel) {
        val currentQty = orderMap[food] ?: 0
        orderMap[food] = currentQty + 1
        notifyListeners()
    }

    fun removeItem(food: FoodModel) {
        val currentQty = orderMap[food] ?: 0
        if (currentQty > 1) {
            orderMap[food] = currentQty - 1
        } else {
            orderMap.remove(food)
        }
        notifyListeners()
    }


    // Method untuk mengatur orderType
    fun setOrderType(orderType: String) {
        currentOrderType = orderType
    }

    // Method untuk mendapatkan orderType
    fun getOrderType(): String {
        return currentOrderType
    }

    fun getQuantity(food: FoodModel): Int {
        return orderMap[food] ?: 0
    }

    fun getOrderMap(): Map<FoodModel, Int> {
        return orderMap.toMap()
    }

    fun getTotalPrice(): Double {
        return orderMap.entries.sumOf { it.key.price * it.value }
    }

    fun isEmpty(): Boolean {
        return orderMap.isEmpty()
    }

    fun clear() {
        orderMap.clear()
        currentOrderType = "delivery"
        notifyListeners()
    }

    fun addOrderChangeListener(listener: () -> Unit) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }


    fun removeOrderChangeListener(listener: () -> Unit) {
        listeners.remove(listener)
    }

    private fun notifyListeners() {
        listeners.forEach { it() }
    }

    fun getOrderSummaryText(): String {
        return orderMap.entries.joinToString("\n") { "${it.key.name} x${it.value}" }
    }
}