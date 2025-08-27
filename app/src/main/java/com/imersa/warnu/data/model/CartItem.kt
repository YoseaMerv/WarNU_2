package com.imersa.warnu.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize // Tambahkan ini
data class CartItem(
    val productId: String? = null,
    val name: String? = null,
    val price: Double? = null,
    var quantity: Int = 0,
    val imageUrl: String? = null,
    val sellerId: String? = null
) : Parcelable // Tambahkan ini