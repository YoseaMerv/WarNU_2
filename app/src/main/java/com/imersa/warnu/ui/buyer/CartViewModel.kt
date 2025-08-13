package com.imersa.warnu.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CartViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cartList = MutableLiveData<List<CartItem>>()
    val cartList: LiveData<List<CartItem>> get() = _cartList

    init {
        listenToCartChanges()
    }

    private fun listenToCartChanges() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("carts")
            .document(userId)
            .collection("items")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _cartList.value = emptyList()
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CartItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                _cartList.value = list
            }
    }

    fun removeFromCart(cartItem: CartItem) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("carts")
            .document(userId)
            .collection("items")
            .document(cartItem.id)
            .delete()
    }
}
