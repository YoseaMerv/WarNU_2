package com.imersa.warnu.ui.seller.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailProductViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product> get() = _product

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun getProductById(productId: String) {
        _loading.value = true
        firestore.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                _loading.value = false
                if (document.exists()) {
                    val product = Product(
                        id = document.id,
                        name = document.getString("name") ?: "",
                        description = document.getString("description") ?: "",
                        category = document.getString("category") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        imageUrl = document.getString("imageUrl") ?: "",
                        sellerId = document.getString("sellerId") ?: "",
                        stock = document.getLong("stock")?.toInt() ?: 0
                    )
                    _product.value = product
                } else {
                    _errorMessage.value = "Produk tidak ditemukan"
                }
            }.addOnFailureListener { e ->
                _loading.value = false
                _errorMessage.value = "Gagal memuat produk: ${e.message}"
            }
    }
}
