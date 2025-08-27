package com.imersa.warnu.data.model

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Model data untuk request body
data class TransactionRequest(
    val orderId: String,
    val totalAmount: Double,
    val items: List<ItemDetails>,
    val customerDetails: CustomerDetails,
    val userId: String?
)

data class ItemDetails(
    val id: String,
    val price: Double,
    val quantity: Int,
    val name: String
)

data class CustomerDetails(
    val first_name: String,
    val email: String,
    val phone: String
)

// Model data untuk response dari backend
data class TransactionResponse(
    val token: String
)

// Interface untuk Retrofit
interface ApiService {
    @POST("create-transaction")
    fun createTransaction(@Body request: TransactionRequest): Call<TransactionResponse>
}