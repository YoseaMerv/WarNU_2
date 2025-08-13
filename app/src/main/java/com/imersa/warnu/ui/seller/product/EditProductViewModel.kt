package com.imersa.warnu.ui.seller.product

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProductViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // LiveData untuk status upload image, berisi URL gambar yang sudah di-upload
    private val _uploadStatus = MutableLiveData<Result<String>>()
    val uploadStatus: LiveData<Result<String>> = _uploadStatus

    // LiveData untuk status update product (sukses/gagal)
    private val _updateStatus = MutableLiveData<Result<Unit>>()
    val updateStatus: LiveData<Result<Unit>> = _updateStatus
    private val _productData = MutableLiveData<Product?>()
    val productData: LiveData<Product?> = _productData

    fun uploadImage(imageUri: Uri) {
        val ref = storage.reference.child("product_images/${System.currentTimeMillis()}.jpg")
        ref.putFile(imageUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    _uploadStatus.postValue(Result.success(uri.toString()))
                }
                    .addOnFailureListener { e ->
                        _uploadStatus.postValue(Result.failure(e))
                    }
            }
            .addOnFailureListener { e ->
                _uploadStatus.postValue(Result.failure(e))
            }
    }

    fun loadProduct(productId: String) {
        firestore.collection("products")
            .document(productId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val product = doc.toObject(Product::class.java)
                    _productData.postValue(product)
                } else {
                    _productData.postValue(null)
                }
            }
            .addOnFailureListener {
                _productData.postValue(null)
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
        val updateData = mapOf(
            "name" to name,
            "description" to description,
            "price" to price,
            "stock" to stock,
            "category" to category,
            "imageUrl" to imageUrl
        )



        firestore.collection("products")
            .document(productId)
            .update(updateData)
            .addOnSuccessListener {
                _updateStatus.postValue(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                _updateStatus.postValue(Result.failure(e))
            }
    }
}
