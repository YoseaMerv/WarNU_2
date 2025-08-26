package com.imersa.warnu.ui.buyer.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.imersa.warnu.data.model.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

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
        val currentQty = item.quantity
        val productId = item.productId ?: return

        if (currentQty <= 1) {
            firestore.collection("carts")
                .document(userId)
                .collection("items")
                .document(productId)
                .delete()
        } else {
            firestore.collection("carts")
                .document(userId)
                .collection("items")
                .document(productId)
                .update("quantity", currentQty - 1)
        }
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
