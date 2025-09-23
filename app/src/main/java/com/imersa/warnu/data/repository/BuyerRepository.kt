package com.imersa.warnu.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.imersa.warnu.data.model.Product

class ProductRepository {

    private val firestore = Firebase.firestore

    // Ambil semua produk (Realtime)
    fun listenProducts(onResult: (List<Product>) -> Unit, onError: (Exception) -> Unit) {
        firestore.collection("products").addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onResult(list)
            }
    }
}
