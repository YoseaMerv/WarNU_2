package com.imersa.warnu.ui.buyer.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.imersa.warnu.data.model.CartItem
import com.imersa.warnu.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeBuyerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage

    private var allProducts: List<Product> = listOf()

    init {
        loadProducts()
    }

    // --- FUNGSI loadProducts YANG DIPERBAIKI ---
    fun loadProducts() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("products").get().await()
                // Ubah cara pengambilan data untuk menyertakan ID dokumen
                val productList = snapshot.documents.mapNotNull { document ->
                    val product = document.toObject(Product::class.java)
                    // Salin objek produk dan tambahkan ID dari dokumen
                    product?.copy(id = document.id)
                }
                allProducts = productList
                _products.postValue(productList)
            } catch (e: Exception) {
                _products.postValue(emptyList())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            _products.value = allProducts
            return
        }
        val filteredList = allProducts.filter { product ->
            product.name?.contains(query, ignoreCase = true) == true
        }
        _products.value = filteredList
    }

    fun addToCart(product: Product) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _toastMessage.value = "You need to be logged in"
            return
        }

        val productId = product.id
        if (productId == null) {
            _toastMessage.value = "Failed to add item: Product ID is missing."
            return
        }

        viewModelScope.launch {
            try {
                val cartRef = firestore.collection("carts").document(userId)
                    .collection("items").document(productId)

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(cartRef)
                    if (snapshot.exists()) {
                        transaction.update(cartRef, "quantity", FieldValue.increment(1))
                    } else {
                        val cartItem = CartItem(
                            productId = product.id,
                            name = product.name,
                            price = product.price,
                            quantity = 1,
                            imageUrl = product.imageUrl,
                            sellerId = product.sellerId
                        )
                        transaction.set(cartRef, cartItem)
                    }
                }.await()

                _toastMessage.postValue("${product.name} added to cart")
            } catch (e: Exception) {
                _toastMessage.postValue("Failed to add item: ${e.message ?: "Unknown Firestore error"}")
            }
        }
    }

    fun onToastMessageShown() {
        _toastMessage.value = null
    }
}