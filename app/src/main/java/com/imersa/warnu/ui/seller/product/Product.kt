package com.imersa.warnu.ui.seller.product

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val sellerId: String = ""
)
