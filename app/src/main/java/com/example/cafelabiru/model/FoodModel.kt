package com.example.cafelabiru.model

import java.io.Serializable

data class FoodModel(
    val menuId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    var count: Int = 1
) :Serializable