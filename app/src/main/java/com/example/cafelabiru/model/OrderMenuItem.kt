package com.example.cafelabiru.model

data class OrderMenuItem(
    val menuId: String = "",
    val menuName: String = "",
    val menuPrice: Int = 0,
    val quantity: Int = 0,
    val imageResId: String = ""
)