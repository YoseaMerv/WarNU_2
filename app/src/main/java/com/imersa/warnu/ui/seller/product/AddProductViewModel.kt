package com.imersa.warnu.ui.seller.product

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val _state = MutableStateFlow<AddProductState>(AddProductState.Idle)
    val state: StateFlow<AddProductState> get() = _state
    fun addProduct(
        name: String?,
        price: String?,
        description: String?,
        stock: String?,
        category: String?,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _state.value = AddProductState.Error("User tidak ditemukan")
                return@launch
            }

            if (imageUri == null) {
                _state.value = AddProductState.Error("Gambar harus dipilih")
                return@launch
            }

            _state.value = AddProductState.Loading

            try {
                val imageRef =
                    storage.reference.child("product_images/${currentUser.uid}/${System.currentTimeMillis()}.jpg")

                imageRef.putFile(imageUri).await()

                val downloadUrl = imageRef.downloadUrl.await()

                val product = hashMapOf(
                    "name" to name,
                    "price" to price?.toDoubleOrNull(),
                    "description" to description,
                    "stock" to stock?.toIntOrNull(),
                    "category" to category,
                    "imageUrl" to downloadUrl.toString(),
                    "sellerId" to currentUser.uid,
                    "createdAt" to System.currentTimeMillis()
                )

                firestore.collection("products").add(product).await()

                _state.value = AddProductState.Success

            } catch (e: Exception) {
                _state.value = AddProductState.Error("Error: ${e.message}")
            }
        }
    }

    fun resetState() {
        _state.value = AddProductState.Idle
    }
}

sealed class AddProductState {
    object Idle : AddProductState()
    object Loading : AddProductState()
    object Success : AddProductState()
    data class Error(val message: String) : AddProductState()
}
