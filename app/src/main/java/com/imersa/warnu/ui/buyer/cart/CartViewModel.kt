package com.imersa.warnu.ui.buyer.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.data.model.CartItem

class CartViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _cartItems.value = emptyList() // Jika tidak login, pastikan keranjang kosong
            return
        }

        // --- PERBAIKAN PATH DI SINI ---
        // Mengakses path yang benar: carts -> {userId} -> items
        db.collection("carts").document(userId).collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error (misal: log ke console)
                    _cartItems.value = emptyList()
                    return@addSnapshotListener
                }

                // Konversi dokumen-dokumen di sub-koleksi 'items' menjadi list CartItem
                val items = snapshot?.toObjects(CartItem::class.java)
                _cartItems.value = items ?: emptyList()
            }
    }

    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        val userId = auth.currentUser?.uid ?: return

        // Mengakses path yang benar untuk update
        db.collection("carts").document(userId).collection("items").document(productId)
            .update("quantity", newQuantity)
            .addOnFailureListener {
                // Handle error
            }
    }

    fun removeCartItem(productId: String) {
        val userId = auth.currentUser?.uid ?: return

        // Mengakses path yang benar untuk hapus
        db.collection("carts").document(userId).collection("items").document(productId)
            .delete()
            .addOnFailureListener {
                // Handle error
            }
    }
}