package com.example.cafelabiru.model

data class OrderModel(
    val orderId: String = "",
    val menuItems: List<OrderMenuItem> = emptyList(), // Changed from single menuIds to list
    val subTotal: Int = 0,
    val ppn: Int = 0,
    val deliveryFee: Int = 7000,
    val total: Int = 0,
    val customerAddress: String = "",
    val customerAddressDetail: String = "",
    val customerPhone: String = "",
    var locationOrderDetail: String? = null,
    val orderDate: Long = System.currentTimeMillis(),
    val orderType: String = "delivery",
    val status: String = "pending" // pending, confirmed, completed
)