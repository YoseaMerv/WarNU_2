package com.imersa.warnu.ui.buyer.order

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.imersa.warnu.data.model.Order

class OrderHistoryViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadOrders()
    }

    private fun loadOrders() {
        _isLoading.value = true
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _orders.value = emptyList()
            _isLoading.value = false
            return
        }

        db.collection("orders").whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null) {
                    // Handle error
                    _orders.value = emptyList()
                    return@addSnapshotListener
                }

                val orderList = snapshot?.toObjects(Order::class.java)
                _orders.value = orderList ?: emptyList()
            }
    }
}