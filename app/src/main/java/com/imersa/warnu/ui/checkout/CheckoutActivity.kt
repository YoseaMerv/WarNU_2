package com.imersa.warnu.ui.checkout

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.R
import com.imersa.warnu.data.model.ApiService
import com.imersa.warnu.data.model.CartItem
import com.imersa.warnu.data.model.CustomerDetails
import com.imersa.warnu.data.model.ItemDetails
import com.imersa.warnu.data.model.Order
import com.imersa.warnu.data.model.TransactionRequest
import com.imersa.warnu.data.model.TransactionResponse
import com.imersa.warnu.data.model.UserProfile
import com.midtrans.sdk.uikit.api.model.TransactionResult
import com.midtrans.sdk.uikit.external.UiKitApi
import com.midtrans.sdk.uikit.internal.util.UiKitConstants
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CheckoutActivity : AppCompatActivity() {

    private val BASE_URL = "http://10.0.2.2:3000/"
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Variabel untuk menyimpan data sementara
    private var currentCartItems: List<CartItem> = listOf()
    private var currentTransactionRequest: TransactionRequest? = null
    private var currentUserProfile: UserProfile? = null

    private val uikitLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val transactionResult = result.data?.getParcelableExtra<TransactionResult>(UiKitConstants.KEY_TRANSACTION_RESULT)
        handleTransactionResult(transactionResult)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        val totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        fetchAllDataAndProceed(totalAmount)
    }

    private fun fetchAllDataAndProceed(totalAmount: Double) {
        val userId = auth.currentUser?.uid ?: run {
            Toast.makeText(this, "Anda harus login untuk melanjutkan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { userDocument ->
                val userProfile = userDocument.toObject(UserProfile::class.java)
                if (userProfile == null) {
                    Toast.makeText(this, "Profil pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }
                this.currentUserProfile = userProfile

                db.collection("carts").document(userId).collection("items").get()
                    .addOnSuccessListener { cartSnapshot ->
                        if (cartSnapshot.isEmpty) {
                            Toast.makeText(this, "Keranjang Anda kosong.", Toast.LENGTH_SHORT).show()
                            finish()
                            return@addOnSuccessListener
                        }
                        val cartItems = cartSnapshot.toObjects(CartItem::class.java)
                        this.currentCartItems = cartItems
                        getSnapToken(totalAmount, cartItems, userProfile)
                    }
                    .addOnFailureListener { e -> handleError("Gagal mengambil item keranjang:", e) }
            }
            .addOnFailureListener { e -> handleError("Gagal mengambil profil pengguna:", e) }
    }

    private fun getSnapToken(totalAmount: Double, cartItems: List<CartItem>, userProfile: UserProfile) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        val itemDetails = cartItems.map {
            ItemDetails(
                id = it.productId ?: "",
                price = it.price ?: 0.0,
                quantity = it.quantity,
                name = it.name ?: "Produk Tanpa Nama"
            )
        }

        val customerDetails = CustomerDetails(
            first_name = userProfile.name ?: "Pengguna",
            email = userProfile.email ?: "",
            phone = userProfile.phone ?: ""
        )

        val orderId = "WARN-ORDER-${System.currentTimeMillis()}"
        val request = TransactionRequest(
            orderId = orderId,
            totalAmount = totalAmount,
            items = itemDetails,
            customerDetails = customerDetails,
            userId = auth.currentUser?.uid
        )
        this.currentTransactionRequest = request

        apiService.createTransaction(request).enqueue(object : Callback<TransactionResponse> {
            override fun onResponse(call: Call<TransactionResponse>, response: Response<TransactionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    startPayment(response.body()!!.token)
                } else {
                    handleApiError("Gagal mendapatkan token: ${response.errorBody()?.string()}")
                }
            }
            override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                handleApiError("Koneksi Error: ${t.message}")
            }
        })
    }

    private fun startPayment(snapToken: String) {
        UiKitApi.Builder()
            .withMerchantClientKey("SB-Mid-client-c3kYeww-hLgqgYq5") // Client Key
            .withContext(this)
            .withMerchantUrl(BASE_URL)
            .enableLog(true)
            .build()

        UiKitApi.getDefaultInstance().startPaymentUiFlow(
            activity = this,
            launcher = uikitLauncher,
            snapToken = snapToken
        )
    }

    private fun handleTransactionResult(result: TransactionResult?) {
        val isSuccess = when (result?.status) {
            UiKitConstants.STATUS_SUCCESS, UiKitConstants.STATUS_PENDING -> true
            else -> false
        }

        val message = result?.status ?: "Transaksi dibatalkan"
        Toast.makeText(this, "Status: $message", Toast.LENGTH_LONG).show()

        if (isSuccess) {
            saveOrderToFirestore(result!!.status)
        } else {
            finish()
        }
    }

    private fun saveOrderToFirestore(paymentStatus: String) {
        val request = currentTransactionRequest
        val user = auth.currentUser
        val profile = currentUserProfile
        if (request == null || user == null || profile == null) {
            Toast.makeText(this, "Data tidak lengkap, gagal menyimpan pesanan.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = FirebaseFirestore.getInstance()

        // 🔎 Pastikan sellerId terisi
        val cartItemsWithSeller = currentCartItems.map { item ->
            if (item.sellerId.isNullOrEmpty() && !item.productId.isNullOrEmpty()) {
                // Ambil sellerId dari koleksi products
                db.collection("products").document(item.productId!!).get()
                    .addOnSuccessListener { doc ->
                        val sellerId = doc.getString("sellerId")
                        item.sellerId = sellerId
                    }
            }
            item
        }

        val order = Order(
            orderId = request.orderId,
            userId = user.uid,
            customerName = profile.name,
            items = cartItemsWithSeller,
            totalAmount = request.totalAmount,
            paymentStatus = paymentStatus,
            createdAt = Timestamp.now(),
            sellerIds = cartItemsWithSeller.mapNotNull { it.sellerId }.distinct()
        )

        db.collection("orders")
            .document(request.orderId!!) // ✅ simpan pakai orderId biar konsisten
            .set(order)
            .addOnSuccessListener {
                Log.d("CheckoutActivity", "Pesanan berhasil disimpan ke Firestore.")
                clearCart()
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                handleError("Gagal menyimpan pesanan ke Firestore:", e)
                finish()
            }
    }


    private fun clearCart() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("carts").document(userId).collection("items")
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit()
                    .addOnSuccessListener { Log.d("CheckoutActivity", "Keranjang berhasil dikosongkan.") }
                    .addOnFailureListener { e -> Log.e("CheckoutActivity", "Gagal mengosongkan keranjang:", e) }
            }
    }

    private fun handleError(message: String, e: Exception) {
        Log.e("CheckoutActivity", message, e)
        Toast.makeText(this, "Terjadi kesalahan, silakan coba lagi.", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun handleApiError(message: String) {
        Log.e("CheckoutActivity", message)
        Toast.makeText(this, "Terjadi kesalahan pada server.", Toast.LENGTH_SHORT).show()
        finish()
    }
}
