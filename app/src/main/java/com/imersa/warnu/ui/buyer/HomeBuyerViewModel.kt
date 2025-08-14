package com.imersa.warnu.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeBuyerViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _products = MutableLiveData<List<ProductBuyer>>()
    val products: LiveData<List<ProductBuyer>> = _products

    private val _filteredProducts = MutableLiveData<List<ProductBuyer>>()
    val filteredProducts: LiveData<List<ProductBuyer>> = _filteredProducts

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _emptyState = MutableLiveData<Boolean>()
    val emptyState: LiveData<Boolean> = _emptyState

    fun loadProducts() {
        _loading.value = true
        firestore.collection("products")
            .addSnapshotListener { snapshot, e ->
                _loading.value = false
                if (e != null) return@addSnapshotListener

                if (snapshot != null && !snapshot.isEmpty) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ProductBuyer::class.java)?.copy(id = doc.id)
                    }
                    _products.value = list
                    _filteredProducts.value = list
                    _emptyState.value = false
                } else {
                    _products.value = emptyList()
                    _filteredProducts.value = emptyList()
                    _emptyState.value = true
                }
            }
    }

    fun searchProducts(query: String) {
        val currentList = _products.value ?: return
        if (query.isBlank()) {
            _filteredProducts.value = currentList
        } else {
            _filteredProducts.value = currentList.filter {
                it.name?.contains(query, ignoreCase = true) == true
            }
        }
    }

    fun addToCart(product: ProductBuyer, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        val productId = product.id ?: return

        val cartRef = firestore.collection("carts")
            .document(userId)
            .collection("items")
            .document(productId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(cartRef)
            if (snapshot.exists()) {
                val currentQty = snapshot.getLong("quantity") ?: 0
                transaction.update(cartRef, "quantity", currentQty + 1)
            } else {
                val cartItem = hashMapOf(
                    "productId" to productId,
                    "name" to product.name,
                    "price" to product.price,
                    "imageUrl" to product.imageUrl,
                    "quantity" to 1
                )
                transaction.set(cartRef, cartItem)
            }
        }.addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
}
