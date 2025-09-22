package com.imersa.warnu.ui.seller.order

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.data.model.Order

class OrderSellerViewModel : ViewModel() {

    private val _orders = MutableLiveData<List<Order>>()
    val orders: LiveData<List<Order>> = _orders

    fun fetchOrders() {
        val sellerId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("orders")
            .whereArrayContains("sellerIds", sellerId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("OrderSellerViewModel", "Error fetching orders", e)
                    _orders.postValue(emptyList())
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orderList = snapshot.toObjects(Order::class.java)
                    _orders.postValue(orderList)
                }
            }
    }
}
