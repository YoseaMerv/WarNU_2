package com.imersa.warnu.data.model

data class Product(
    val id: String? = null,
    val sellerId: String? = null,
    val name: String? = null,
    val price: Double? = null,
    val description: String? = null,
    val stock: Int? = null,
    val imageUrl: String? = null,
    val category: String? = null,
    val storeName: String? = null
) {
    // Constructor kosong diperlukan untuk Firestore
    constructor() : this(null, null, null, null, null, null, null, null, null)
}