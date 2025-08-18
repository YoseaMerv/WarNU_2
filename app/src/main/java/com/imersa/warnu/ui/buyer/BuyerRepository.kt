package com.imersa.warnu.ui.buyer


import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.imersa.warnu.ui.buyer.ProductBuyer
import kotlinx.coroutines.tasks.await

class ProductRepository {

    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    // Ambil semua produk (Realtime)
    fun listenProducts(onResult: (List<ProductBuyer>) -> Unit, onError: (Exception) -> Unit) {
        firestore.collection("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    onError(e)
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ProductBuyer::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                onResult(list)
            }
    }

    // Tambah ke keranjang
    suspend fun addToCart(product: ProductBuyer) {
        val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
        val productId = product.id ?: throw Exception("Product ID not found")

        val cartRef = firestore.collection("carts")
            .document(userId)
            .collection("items")
            .document(productId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(cartRef)
            if (snapshot.exists()) {
                val currentQty = snapshot.getLong("quantity") ?: 0
                transaction.update(cartRef, "quantity", currentQty + 1)
            } else {
                val cartItem = hashMapOf(
                    "productId" to productId,
                    "name" to product.name,
                    "price" to product.price,
                    "imageUrl" to product.imageUrl,
                    "quantity" to 1
                )
                transaction.set(cartRef, cartItem)
            }
        }.await()
    }
}
