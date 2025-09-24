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
import com.imersa.warnu.data.model.CartItem
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

@AndroidEntryPoint
class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val client = OkHttpClient()
    private val gson = Gson()

    private val cartViewModel: CartViewModel by viewModels()


    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var firestore: FirebaseFirestore

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Tampilkan ProgressBar di awal
        binding.progressBar.visibility = View.VISIBLE
        setupWebView()

        val cartItemsJson = intent.getStringExtra("CART_ITEMS")
        if (cartItemsJson != null) {
            val itemType = object : TypeToken<List<CartItem>>() {}.type
            val cartItems: List<CartItem> = gson.fromJson(cartItemsJson, itemType)
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
                // Halaman mulai dimuat, sembunyikan ProgressBar
                binding.progressBar.visibility = View.GONE
            }

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                Log.d("WebViewRedirect", "Redirecting to: $url")
                if (url != null) {
                    // Cek URL callback dari Midtrans
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

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val userName = document.getString("name") ?: "Customer"
                val userEmail = document.getString("email") ?: ""

                val customerDetails = mapOf("first_name" to userName, "email" to userEmail)
                val itemDetails = cartItems.map {
                    mapOf(
                        "id" to it.productId,
                        "price" to it.price,
                        "quantity" to it.quantity,
                        "name" to it.name
                    )
                }
                val sellerId = cartItems.firstOrNull()?.sellerId

                val transactionRequest = mapOf(
                    "orderId" to orderId,
                    "totalAmount" to totalAmount,
                    "items" to itemDetails,
                    "customerDetails" to customerDetails,
                    "userId" to userId,
                    "sellerId" to sellerId
                )

                // Pastikan IP address sudah benar (10.0.2.2 untuk emulator standar)
                val request = Request.Builder()
                    .url("http://10.0.2.2:3000/create-transaction")
                    .post(gson.toJson(transactionRequest).toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("CheckoutActivity", "onFailure: ${e.message}")
                        runOnUiThread {
                            Toast.makeText(this@CheckoutActivity, "Failed to connect to server. Please check your connection and server status.", Toast.LENGTH_LONG).show()
                            finish()
                        }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        if (response.isSuccessful) {
                            val responseBody = response.body?.string()
                            if (responseBody != null) {
                                try {
                                    val token = JSONObject(responseBody).getString("token")
                                    if (token.isNotEmpty()) {
                                        runOnUiThread {
                                            val midtransUrl = "https://app.sandbox.midtrans.com/snap/v2/vtweb/$token"
                                            binding.webView.loadUrl(midtransUrl)
                                        }
                                    } else {
                                        // Kasus jika token kosong
                                        runOnUiThread {
                                            Toast.makeText(this@CheckoutActivity, "Failed to get payment token.", Toast.LENGTH_SHORT).show()
                                            finish()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("CheckoutActivity", "Error parsing JSON: ${e.message}")
                                    runOnUiThread {
                                        Toast.makeText(this@CheckoutActivity, "Invalid response from server.", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                }
                            }
                        } else {
                            Log.e("CheckoutActivity", "onResponse but not successful. Code: ${response.code}")
                            runOnUiThread {
                                Toast.makeText(this@CheckoutActivity, "Server returned an error.", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }
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