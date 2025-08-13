package com.imersa.warnu.ui.buyer

data class CartItem(
    val id: String = "",
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String = ""
)
