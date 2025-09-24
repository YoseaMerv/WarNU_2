package com.imersa.warnu.ui.seller.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.data.model.CartItem
import com.imersa.warnu.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class DetailProductViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product> = _product

    private val _addToCartStatus = MutableLiveData<String?>()
    val addToCartStatus: LiveData<String?> = _addToCartStatus

    private val _userRole = MutableLiveData<String?>()
    val userRole: LiveData<String?> = _userRole

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            try {
                val document = firestore.collection("products").document(productId).get().await()
                // Menggunakan .toObject dan menyalin ID secara manual
                val productData = document.toObject(Product::class.java)?.copy(id = document.id)
                _product.postValue(productData)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun loadUserRole() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _userRole.value = null // Set role ke null jika tidak ada user
            return
        }
        viewModelScope.launch {
            try {
                val document = firestore.collection("users").document(userId).get().await()
                _userRole.postValue(document.getString("role"))
            } catch (e: Exception) {
                _userRole.postValue(null)
            }
        }
    }

    fun addToCart(product: Product, quantity: Int) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _addToCartStatus.value = "You must be logged in to add items."
            return
        }
        val productId = product.id
        if (productId == null) {
            _addToCartStatus.value = "Cannot add item: Product ID is missing."
            return
        }

        viewModelScope.launch {
            try {
                val cartRef = firestore.collection("carts").document(userId)
                    .collection("items").document(productId)

                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(cartRef)
                    if (snapshot.exists()) {
                        // FieldValue.increment membutuhkan Long
                        transaction.update(cartRef, "quantity", FieldValue.increment(quantity.toLong()))
                    } else {
                        val cartItem = CartItem(
                            productId = product.id,
                            name = product.name,
                            price = product.price,
                            quantity = quantity,
                            imageUrl = product.imageUrl,
                            sellerId = product.sellerId
                        )
                        transaction.set(cartRef, cartItem)
                    }
                }.await()
                _addToCartStatus.postValue("Successfully added to cart.")
            } catch (e: Exception) {
                _addToCartStatus.postValue("Failed to add to cart: ${e.message}")
            }
        }
    }

    fun onStatusMessageShown() {
        _addToCartStatus.value = null
    }
}