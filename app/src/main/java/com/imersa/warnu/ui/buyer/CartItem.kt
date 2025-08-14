package com.imersa.warnu.ui.buyer

data class CartItem(
    val id: String? = null,
    val productId: String? = null,
    val name: String? = null,
    val price: Double? = null,
    val imageUrl: String? = null,
    var quantity: Int = 0
)

