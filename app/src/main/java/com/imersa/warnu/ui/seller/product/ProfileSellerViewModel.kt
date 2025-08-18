package com.imersa.warnu.ui.seller.product

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileSellerViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // LiveData untuk data profil seller
    private val _sellerName = MutableLiveData<String>()
    val sellerName: LiveData<String> get() = _sellerName

    private val _sellerEmail = MutableLiveData<String>()
    val sellerEmail: LiveData<String> get() = _sellerEmail

    private val _storeName = MutableLiveData<String>()
    val storeName: LiveData<String> get() = _storeName

    private val _phone = MutableLiveData<String>()
    val phone: LiveData<String> get() = _phone

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    private val _photoUrl = MutableLiveData<String>()
    val photoUrl: LiveData<String> get() = _photoUrl

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun loadSellerProfile() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _sellerName.value = doc.getString("name") ?: "Toko"
                    _sellerEmail.value = doc.getString("email") ?: "toko1@gmail.com"
                    _storeName.value = doc.getString("storeName") ?: "Toko1"
                    _phone.value = doc.getString("phone") ?: "1234567"
                    _address.value = doc.getString("address") ?: "Jakarta"
                    _photoUrl.value = doc.getString("photoUrl") ?: ""
                    _errorMessage.value = null
                } else {
                    _errorMessage.value = "Data profil tidak ditemukan"
                }
            }
            .addOnFailureListener { e ->
                _errorMessage.value = e.message ?: "Gagal memuat data profil"
            }
            .addOnCompleteListener {
                _isLoading.value = false
            }
    }
}
