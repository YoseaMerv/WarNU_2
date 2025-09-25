// CheckoutActivity.kt

package com.imersa.warnu.ui.checkout

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.imersa.warnu.data.model.*
import com.imersa.warnu.databinding.ActivityCheckoutBinding
import com.imersa.warnu.ui.buyer.cart.CartViewModel
import com.imersa.warnu.ui.buyer.main.MainBuyerActivity
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.*
import javax.inject.Inject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val cartViewModel: CartViewModel by viewModels()

    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore

    // Gunakan Retrofit untuk komunikasi yang lebih bersih
    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            // PENTING: Ganti dengan URL backend Anda yang sudah di-deploy
            .baseUrl("https://warnu-f1434.et.r.appspot.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.progressBar.visibility = View.VISIBLE
        setupWebView()

        val cartItemsJson = intent.getStringExtra("CART_ITEMS")
        if (cartItemsJson != null) {
            val itemType = object : TypeToken<List<CartItem>>() {}.type
            val cartItems: List<CartItem> = Gson().fromJson(cartItemsJson, itemType)
            startCheckout(cartItems)
        } else {
            Toast.makeText(this, "Cart is empty.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupWebView() {
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                binding.progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url != null) {
                    if (url.contains("finish")) {
                        navigateToHome("Payment Successful")
                        return true
                    } else if (url.contains("unfinish")) {
                        navigateToHome("Payment Pending")
                        return true
                    } else if (url.contains("error")) {
                        navigateToHome("Payment Failed")
                        return true
                    }
                }
                return false
            }
        }
    }

    private fun startCheckout(cartItems: List<CartItem>) {
        val totalAmount = cartItems.sumOf { (it.price ?: 0.0) * it.quantity }
        val orderId = "order-${UUID.randomUUID()}"
        val userId = auth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Ambil sellerId dari item pertama di keranjang
        val sellerId = cartItems.firstOrNull()?.sellerId
        if (sellerId == null) {
            Toast.makeText(this, "Seller information is missing.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name")
                val userEmail = document.getString("email")
                val userPhone = document.getString("phone")

                val customerDetails = CustomerDetails(
                    first_name = userName,
                    email = userEmail,
                    phone = userPhone
                )

                val itemDetails = cartItems.map {
                    ItemDetails(
                        id = it.productId!!,
                        price = it.price!!,
                        quantity = it.quantity,
                        name = it.name!!
                    )
                }

                val transactionRequest = TransactionRequest(
                    orderId = orderId,
                    totalAmount = totalAmount,
                    items = itemDetails,
                    customerDetails = customerDetails,
                    userId = userId,
                    sellerId = sellerId
                )

                // Memanggil API menggunakan Retrofit
                apiService.createTransaction(transactionRequest).enqueue(object : retrofit2.Callback<TransactionResponse> {
                    override fun onResponse(call: retrofit2.Call<TransactionResponse>, response: retrofit2.Response<TransactionResponse>) {
                        if (response.isSuccessful) {
                            val token = response.body()?.token
                            if (!token.isNullOrEmpty()) {
                                val midtransUrl = "https://app.sandbox.midtrans.com/snap/v2/vtweb/$token"
                                binding.webView.loadUrl(midtransUrl)
                            } else {
                                Toast.makeText(this@CheckoutActivity, "Failed to get payment token.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        } else {
                            Log.e("CheckoutActivity", "Server returned an error. Code: ${response.code()}")
                            Toast.makeText(this@CheckoutActivity, "Server returned an error.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<TransactionResponse>, t: Throwable) {
                        Log.e("CheckoutActivity", "onFailure: ${t.message}")
                        Toast.makeText(this@CheckoutActivity, "Failed to connect to server.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                })
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get user data.", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun navigateToHome(message: String) {
        if (!isFinishing) {
            if (message == "Payment Successful") {
                cartViewModel.clearCart()
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainBuyerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}