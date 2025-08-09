package com.imersa.warnu.ui.seller.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class ProductRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun getProductsBySeller(sellerId: String): LiveData<List<Product>> {
        val result = MutableLiveData<List<Product>>()

        firestore.collection("products")
            .whereEqualTo("sellerId", sellerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    result.value = emptyList()
                    return@addSnapshotListener
                }

                val products = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Product::class.java)?.copy(id = doc.id)
                } ?: emptyList()

                result.value = products
            }

        return result
    }

    fun deleteProduct(productId: String, onResult: (Boolean) -> Unit) {
        firestore.collection("products").document(productId)
            .delete()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
