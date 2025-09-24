package com.imersa.warnu.ui.seller.product

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.imersa.warnu.data.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AddProductState {
    object Idle : AddProductState()
    object Loading : AddProductState()
    object Success : AddProductState()
    data class Error(val message: String) : AddProductState()
}

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _state = MutableLiveData<AddProductState>(AddProductState.Idle)
    val state: LiveData<AddProductState> = _state

    fun addProduct(
        name: String,
        priceStr: String,
        stockStr: String,
        category: String,
        description: String,
        imageUri: Uri?
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            _state.value = AddProductState.Error("User not authenticated.")
            return
        }
        if (name.isBlank() || priceStr.isBlank() || stockStr.isBlank() || category.isBlank() || description.isBlank() || imageUri == null) {
            _state.value = AddProductState.Error("All fields must be filled and image must be selected.")
            return
        }

        _state.value = AddProductState.Loading

        viewModelScope.launch {
            try {
                val imageUrl = uploadProductImage(userId, imageUri)
                saveProductToFirestore(userId, name, priceStr.toDouble(), stockStr.toInt(), category, description, imageUrl)
                _state.postValue(AddProductState.Success)
            } catch (e: Exception) {
                _state.postValue(AddProductState.Error(e.message ?: "An unknown error occurred."))
            }
        }
    }

    private suspend fun uploadProductImage(userId: String, imageUri: Uri): String {
        val fileName = "${System.currentTimeMillis()}_${userId}"
        val storageRef = storage.reference.child("product_images/$fileName")
        val uploadTask = storageRef.putFile(imageUri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    private suspend fun saveProductToFirestore(
        userId: String, name: String, price: Double, stock: Int, category: String, description: String, imageUrl: String
    ) {
        val newProduct = Product(
            sellerId = userId,
            name = name,
            price = price,
            stock = stock,
            category = category,
            description = description,
            imageUrl = imageUrl
        )
        // Firestore akan generate ID otomatis
        firestore.collection("products").add(newProduct).await()
    }

    fun resetState() {
        _state.value = AddProductState.Idle
    }
}