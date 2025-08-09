package com.imersa.warnu.ui.seller.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<AddProductState>(AddProductState.Idle)
    val state: StateFlow<AddProductState> get() = _state

    fun addProduct(
        name: String?,
        price: String?,
        description: String?,
        stock: String?,
        category: String?,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _state.value = AddProductState.Error("User tidak ditemukan")
                return@launch
            }

            val product = hashMapOf(
                "name" to name,
                "price" to price?.toDoubleOrNull(),
                "description" to description,
                "stock" to stock?.toIntOrNull(),
                "category" to category,
                "imageUrl" to imageUrl,
                "sellerId" to currentUser.uid,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("products")
                .add(product)
                .addOnSuccessListener {
                    _state.value = AddProductState.Success
                }
                .addOnFailureListener {
                    _state.value = AddProductState.Error("Gagal menambahkan produk")
                }
        }
    }

    fun resetState() {
        _state.value = AddProductState.Idle
    }
}

sealed class AddProductState {
    object Idle : AddProductState()
    object Success : AddProductState()
    data class Error(val message: String) : AddProductState()
}
