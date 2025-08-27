package com.imersa.warnu.ui.checkout

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.R
import com.imersa.warnu.data.model.ApiService
import com.imersa.warnu.data.model.CartItem
import com.imersa.warnu.data.model.CustomerDetails
import com.imersa.warnu.data.model.ItemDetails
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
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Anda harus login untuk melanjutkan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val userProfileRef = db.collection("users").document(userId)
        userProfileRef.get().addOnSuccessListener { userDocument ->
            if (userDocument == null || !userDocument.exists()) {
                Toast.makeText(this, "Profil pengguna tidak ditemukan.", Toast.LENGTH_SHORT).show()
                finish()
                return@addOnSuccessListener
            }
            val userProfile = userDocument.toObject(UserProfile::class.java)

            db.collection("carts").document(userId).collection("items").get()
                .addOnSuccessListener { cartSnapshot ->
                    if (cartSnapshot.isEmpty) {
                        Toast.makeText(this, "Keranjang Anda kosong.", Toast.LENGTH_SHORT).show()
                        finish()
                        return@addOnSuccessListener
                    }
                    val cartItems = cartSnapshot.toObjects(CartItem::class.java)
                    getSnapToken(totalAmount, cartItems, userProfile)
                }
                .addOnFailureListener { e ->
                    Log.e("CheckoutActivity", "Gagal mengambil item keranjang:", e)
                    Toast.makeText(this, "Gagal memuat data keranjang.", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }.addOnFailureListener { e ->
            Log.e("CheckoutActivity", "Gagal mengambil profil pengguna:", e)
            Toast.makeText(this, "Gagal memuat profil pengguna.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun getSnapToken(totalAmount: Double, cartItems: List<CartItem>, userProfile: UserProfile?) {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(ApiService::class.java)

        val itemDetails = cartItems.map {
            // PERBAIKAN 1: Gunakan 'productName' sesuai model data CartItem
            ItemDetails(
                id = it.productId ?: "",
                price = it.price ?: 0.0,
                quantity = it.quantity,
                name = it.name ?: "Produk Tanpa Nama"
            )
        }

        val customerDetails = CustomerDetails(
            // PERBAIKAN 2: Gunakan 'fullName' sesuai model data UserProfile
            first_name = userProfile?.name ?: "Pengguna WarNU",
            email = userProfile?.email ?: "email@tidakada.com",
            phone = userProfile?.phone ?: "08123456789"
        )

        val firebaseUser = auth.currentUser
        val request = TransactionRequest(
            orderId = "WARNU-ORDER-" + System.currentTimeMillis(),
            totalAmount = totalAmount,
            items = itemDetails,
            customerDetails = customerDetails,
            userId = firebaseUser?.uid
        )

        apiService.createTransaction(request).enqueue(object : Callback<TransactionResponse> {
            override fun onResponse(call: Call<TransactionResponse>, response: Response<TransactionResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    startPayment(response.body()!!.token)
                } else {
                    Log.e("CheckoutActivity", "Gagal mendapatkan token: ${response.errorBody()?.string()}")
                    Toast.makeText(this@CheckoutActivity, "Gagal mendapatkan token pembayaran.", Toast.LENGTH_SHORT).show()
                    finish() // Tutup jika gagal dapat token
                }
            }
            override fun onFailure(call: Call<TransactionResponse>, t: Throwable) {
                Log.e("CheckoutActivity", "Koneksi Error: ${t.message}")
                Toast.makeText(this@CheckoutActivity, "Koneksi ke server gagal.", Toast.LENGTH_SHORT).show()
                finish() // Tutup jika koneksi gagal
            }
        })
    }

    private fun startPayment(snapToken: String) {
        UiKitApi.Builder()
            .withMerchantClientKey("MASUKKAN_CLIENT_KEY_ANDA")
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
        if (result == null) {
            Toast.makeText(this, "Transaksi dibatalkan", Toast.LENGTH_LONG).show()
        } else {
            val message: String

            // PERBAIKAN 3: Gunakan STATUS_SUCCESS dan kosongkan keranjang saat PENDING juga
            when (result.status) {
                UiKitConstants.STATUS_SUCCESS, UiKitConstants.STATUS_PENDING -> {
                    message = "Pesanan berhasil dibuat!"
                    // Kosongkan keranjang karena pesanan sudah dibuat
                    clearCart()
                }
                UiKitConstants.STATUS_FAILED -> {
                    message = "Transaksi Gagal: ${result.transactionId}"
                }
                UiKitConstants.STATUS_CANCELED -> {
                    message = "Transaksi Dibatalkan"
                }
                else -> {
                    message = "Status Transaksi Tidak Valid"
                }
            }
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        // Selalu kirim sinyal dan tutup activity setelah proses selesai
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun clearCart() {
        val userId = auth.currentUser?.uid ?: return
        val cartItemsRef = db.collection("carts").document(userId).collection("items")

        cartItemsRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.isEmpty) return@addOnSuccessListener
            val batch = db.batch()
            for (document in snapshot.documents) {
                batch.delete(document.reference)
            }
            batch.commit()
                .addOnSuccessListener { Log.d("CheckoutActivity", "Keranjang berhasil dikosongkan.") }
                .addOnFailureListener { e -> Log.e("CheckoutActivity", "Gagal mengosongkan keranjang:", e) }
        }
    }
}