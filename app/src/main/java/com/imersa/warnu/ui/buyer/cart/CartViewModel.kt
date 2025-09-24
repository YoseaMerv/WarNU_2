package com.imersa.warnu.ui.buyer.cart

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.imersa.warnu.data.model.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

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

        // Menggunakan struktur database Anda: "carts/{userId}/items"
        firestore.collection("carts").document(userId).collection("items")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _cartItems.value = emptyList()
                    return@addSnapshotListener
                }

                val items = snapshot?.toObjects(CartItem::class.java)
                _cartItems.value = items ?: emptyList()
            }
    }

    // Nama fungsi disesuaikan dengan kode Anda
    fun updateCartItemQuantity(productId: String, newQuantity: Int) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("carts").document(userId).collection("items").document(productId)
            .update("quantity", newQuantity).addOnFailureListener {
                // Handle failure if needed
            }
    }

    // Nama fungsi disesuaikan dengan kode Anda
    fun removeCartItem(productId: String) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("carts").document(userId).collection("items").document(productId).delete()
            .addOnFailureListener {
                // Handle failure if needed
            }
    }
    fun clearCart() {
        val userId = auth.currentUser?.uid
        if (userId == null) return

        viewModelScope.launch {
            try {
                val cartItemsSnapshot = firestore.collection("carts").document(userId)
                    .collection("items").get().await()

                // Gunakan batch write untuk menghapus semua dokumen secara efisien
                val batch: WriteBatch = firestore.batch()
                for (document in cartItemsSnapshot.documents) {
                    batch.delete(document.reference)
                }
                batch.commit().await()
            } catch (e: Exception) {
                // Handle error jika diperlukan
            }
        }
    }
}