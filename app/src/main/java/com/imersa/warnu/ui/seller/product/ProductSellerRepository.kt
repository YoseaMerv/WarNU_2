package com.imersa.warnu.ui.seller.product

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import javax.inject.Inject

class ProductSellerRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
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
        // Ambil data produk dulu untuk mengetahui image URL
        firestore.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                val imageUrl = document.getString("imageUrl")

                val deleteDoc = {
                    firestore.collection("products").document(productId)
                        .delete()
                        .addOnSuccessListener { onResult(true) }
                        .addOnFailureListener { onResult(false) }
                }

                if (!imageUrl.isNullOrEmpty()) {
                    val storageRef = storage.getReferenceFromUrl(imageUrl)
                    storageRef.delete()
                        .addOnSuccessListener { deleteDoc() }
                        .addOnFailureListener {
                            deleteDoc()
                        }
                } else {
                    deleteDoc()
                }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}
