package com.imersa.warnu.ui.seller.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.ui.buyer.ProductBuyer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailProductViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product> get() = _product

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> get() = _loading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage
    fun addToCart(productId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Referensi ke subkoleksi items milik user
        val itemsRef = firestore.collection("carts")
            .document(userId)
            .collection("items")

        // Cek apakah produk sudah ada di keranjang user
        itemsRef.whereEqualTo("productId", productId)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Produk sudah ada → update quantity
                    val docId = documents.documents[0].id
                    val currentQty = documents.documents[0].getLong("quantity") ?: 1
                    itemsRef.document(docId)
                        .update("quantity", currentQty + 1)
                        .addOnSuccessListener {
                            _errorMessage.value = "Jumlah produk di keranjang bertambah"
                        }
                        .addOnFailureListener {
                            _errorMessage.value = "Gagal menambah jumlah produk"
                        }
                } else {
                    // Produk belum ada → tambah baru
                    val cartItem = hashMapOf(
                        "productId" to productId,
                        "quantity" to 1
                    )
                    itemsRef.add(cartItem)
                        .addOnSuccessListener {
                            _errorMessage.value = "Produk berhasil ditambahkan ke keranjang"
                        }
                        .addOnFailureListener {
                            _errorMessage.value = "Gagal menambahkan produk ke keranjang"
                        }
                }
            }
            .addOnFailureListener {
                _errorMessage.value = "Gagal memeriksa keranjang"
            }
    }

    fun getProductById(productId: String) {
        _loading.value = true
        firestore.collection("products")
            .document(productId)
            .get()
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
            }
            .addOnFailureListener { e ->
                _loading.value = false
                _errorMessage.value = "Gagal memuat produk: ${e.message}"
            }
    }
}
