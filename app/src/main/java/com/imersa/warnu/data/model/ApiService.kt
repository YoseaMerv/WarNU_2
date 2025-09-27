// app/src/main/java/com/imersa/warnu/data/model/ApiService.kt
package com.imersa.warnu.data.model

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class CustomerDetails(
    val name: String?,
    val email: String?,
    val phone: String?
)

data class ItemDetails(
    val id: String,
    val price: Double,
    val quantity: Int,
    val name: String,
    val imageUrl: String?,
    val sellerId: String?, // Tambahkan field ini
    val storeName: String? // Tambahkan field ini
)

data class MultiVendorTransactionRequest(
    val allItems: List<ItemDetails>,
    val customerDetails: CustomerDetails,
    val userId: String,
    val address: String?
)

data class TransactionResponse(
    val token: String
)

interface ApiService {
    @POST("create-multivendor-transaction")
    fun createMultiVendorTransaction(@Body request: MultiVendorTransactionRequest): Call<TransactionResponse>
}