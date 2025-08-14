package com.imersa.warnu.ui.buyer

data class ProductBuyer(
    val id: String? = null,
    val sellerId: String? = null,
    val name: String? = null,
    val price: Double? = null,
    val description: String? = null,
    val stock: Int? = 0,
    val imageUrl: String? = null,
    val category: String? = null
)
