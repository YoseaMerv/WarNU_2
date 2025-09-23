package com.imersa.warnu.ui.seller.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.imersa.warnu.data.model.Order

class OrderSellerViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _emptyState = MutableLiveData<Boolean>()
    val emptyState: LiveData<Boolean> = _emptyState

    fun loadOrders() {
        _loading.value = true
        val currentSellerId = auth.currentUser?.uid

        if (currentSellerId == null) {
            _loading.value = false
            _emptyState.value = true
            return
        }

        db.collection("orders").whereEqualTo("sellerId", currentSellerId)
            .orderBy("createdAt", Query.Direction.DESCENDING) // Tampilkan yang terbaru di atas
            .get().addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    _emptyState.value = true
                } else {
                    val orderList = documents.toObjects(Order::class.java)
                    _orders.value = orderList
                    _emptyState.value = false
                }
                _loading.value = false
            }.addOnFailureListener {
                _loading.value = false
                _emptyState.value = true
            }
    }
}