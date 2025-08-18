package com.imersa.warnu.ui.seller.product

data class Product(
    val id: String? = null,
    val sellerId: String? = null,
    val name: String? = null,
    val price: Double? = null,
    val description: String? = null,
    val stock: Int? = 0,
    val imageUrl: String? = null,
    val category: String? = null
)

data class SellerProfile(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val storeName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val photoUrl: String? = null
)
