package com.imersa.warnu.ui.seller.product

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.imersa.warnu.data.model.Product

class EditProductViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _productData = MutableLiveData<Product?>()
    val productData: LiveData<Product?> get() = _productData

    private val _uploadStatus = MutableLiveData<Result<String>>()
    val uploadStatus: LiveData<Result<String>> get() = _uploadStatus

    private val _updateStatus = MutableLiveData<Result<Unit>>()
    val updateStatus: LiveData<Result<Unit>> get() = _updateStatus

    fun loadProduct(productId: String) {
        db.collection("products").document(productId).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val product = snapshot.toObject(Product::class.java)
                    _productData.postValue(product)
                } else {
                    _productData.postValue(null)
                }
            }.addOnFailureListener {
                _productData.postValue(null)
            }
    }

    fun uploadImageAndDeleteOld(imageUri: Uri, oldImageUrl: String?, sellerUid: String) {
        if (!oldImageUrl.isNullOrEmpty()) {
            try {
                val oldRef = storage.getReferenceFromUrl(oldImageUrl)
                oldRef.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val fileName = "${System.currentTimeMillis()}.jpg"
        val ref = storage.reference.child("product_images/$sellerUid/$fileName")

        ref.putFile(imageUri).addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    _uploadStatus.postValue(Result.success(uri.toString()))
                }.addOnFailureListener { e ->
                    _uploadStatus.postValue(Result.failure(e))
                }
            }.addOnFailureListener { e ->
                _uploadStatus.postValue(Result.failure(e))
            }
    }

    fun updateProduct(
        productId: String,
        name: String,
        description: String,
        price: Double,
        stock: Long,
        category: String,
        imageUrl: String
    ) {
        val productData = mapOf(
            "name" to name,
            "description" to description,
            "price" to price,
            "stock" to stock,
            "category" to category,
            "imageUrl" to imageUrl
        )

        db.collection("products").document(productId).update(productData).addOnSuccessListener {
                _updateStatus.postValue(Result.success(Unit))
            }.addOnFailureListener { e ->
                _updateStatus.postValue(Result.failure(e))
            }
    }
}
