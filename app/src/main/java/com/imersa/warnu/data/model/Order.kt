package com.imersa.warnu.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Order(
    val orderId: String? = null,
    val userId: String? = null,
    val totalAmount: Double? = null,
    val paymentStatus: String? = null, // "pending", "settlement", "failed"
    val createdAt: Timestamp? = null,
    @get:PropertyName("items") // Pastikan nama properti cocok dengan di Firestore
    @set:PropertyName("items")
    var items: List<CartItem>? = null, // Menggunakan kembali CartItem
    val customerName: String? = null
)