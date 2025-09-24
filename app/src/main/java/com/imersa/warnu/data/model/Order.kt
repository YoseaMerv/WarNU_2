package com.imersa.warnu.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Order(
    val orderId: String? = null,
    val userId: String? = null,
    val totalAmount: Double? = null,
    val paymentStatus: String? = null,
    val createdAt: Timestamp? = null,
    val customerName: String? = null,
    val sellerId: String? = null,

    @get:PropertyName("items")
    @set:PropertyName("items")
    var items: List<CartItem>? = null
)