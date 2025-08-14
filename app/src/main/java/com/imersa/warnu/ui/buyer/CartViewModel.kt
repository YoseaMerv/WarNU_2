package com.imersa.warnu.ui.buyer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CartViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> get() = _cartItems

    private var listenerRegistration: ListenerRegistration? = null

    fun listenCartData() {
        val userId = auth.currentUser?.uid ?: return
        listenerRegistration?.remove() // hapus listener lama

        listenerRegistration = firestore.collection("carts")
            .document(userId)
            .collection("items")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                val items = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CartItem::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                _cartItems.value = items
            }
    }

    fun increaseQty(item: CartItem) {
        val userId = auth.currentUser?.uid ?: return
        val newQty = item.quantity + 1
        firestore.collection("carts")
            .document(userId)
            .collection("items")
            .document(item.productId ?: return)
            .update("quantity", newQty)
    }

    fun decreaseQty(item: CartItem) {
        val userId = auth.currentUser?.uid ?: return
        val newQty = (item.quantity - 1).coerceAtLeast(1)
        firestore.collection("carts")
            .document(userId)
            .collection("items")
            .document(item.productId ?: return)
            .update("quantity", newQty)
    }

    fun removeFromCart(item: CartItem) {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("carts")
            .document(userId)
            .collection("items")
            .document(item.productId ?: return)
            .delete()
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
