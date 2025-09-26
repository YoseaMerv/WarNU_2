// ApiService.kt

package com.imersa.warnu.data.model

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class CustomerDetails(
    val first_name: String?,
    val email: String?,
    val phone: String?
)

data class ItemDetails(
    val id: String,
    val price: Double,
    val quantity: Int,
    val name: String
)

data class TransactionRequest(
    val orderId: String,
    val totalAmount: Double,
    val items: List<ItemDetails>,
    val customerDetails: CustomerDetails,
    val userId: String,
    val sellerId: String,
    val address: String?
)

data class TransactionResponse(
    val token: String
)

interface ApiService {
    @POST("create-transaction")
    fun createTransaction(@Body request: TransactionRequest): Call<TransactionResponse>
}