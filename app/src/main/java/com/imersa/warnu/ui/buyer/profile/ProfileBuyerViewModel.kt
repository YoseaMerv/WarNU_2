package com.imersa.warnu.ui.buyer.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileBuyerViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // LiveData untuk data profil buyer
    private val _buyerName = MutableLiveData<String>()
    val buyerName: LiveData<String> get() = _buyerName

    private val _buyerEmail = MutableLiveData<String>()
    val buyerEmail: LiveData<String> get() = _buyerEmail

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

    fun loadBuyerProfile() {
        _isLoading.value = true
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    _buyerName.value = doc.getString("name") ?: "Pengguna"
                    _buyerEmail.value = doc.getString("email") ?: "email@example.com"
                    _phone.value = doc.getString("phone") ?: "No. Telepon"
                    _address.value = doc.getString("address") ?: "Alamat"
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