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
            _cartItems.value = emptyList()
            return
        }

        db.collection("carts").document(userId).collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _cartItems.value = emptyList()
                    return@addSnapshotListener
                }

                val items = snapshot?.toObjects(CartItem::class.java)
                _cartItems.value = items ?: emptyList()
            }
    }

    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("carts").document(userId).collection("items").document(productId)
            .update("quantity", newQuantity).addOnFailureListener {}
    }

    fun removeCartItem(productId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("carts").document(userId).collection("items").document(productId).delete()
            .addOnFailureListener {}
    }
}