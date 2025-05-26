package com.example.cafelabiru

import com.example.cafelabiru.model.FoodModel

object OrderManager {
    private val orderMap = mutableMapOf<FoodModel, Int>()
    private val listeners = mutableListOf<() -> Unit>()

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
        notifyListeners()
    }

    fun addOrderChangeListener(listener: () -> Unit) {
        listeners.add(listener)
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