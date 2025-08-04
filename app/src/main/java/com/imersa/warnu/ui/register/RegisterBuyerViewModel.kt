package com.imersa.warnu.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RegisterBuyerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _isRegisterSuccess = MutableLiveData<Boolean>()
    val isRegisterSuccess: LiveData<Boolean> get() = _isRegisterSuccess

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun registerBuyer(
        fullName: String,
        address: String,
        email: String,
        phone: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid.orEmpty()
                val userData = mapOf(
                    "uid" to userId,
                    "fullName" to fullName,
                    "address" to address,
                    "email" to email,
                    "phone" to phone,
                    "role" to "buyer"
                )

                firestore.collection("users").document(userId)
                    .set(userData)
                    .addOnSuccessListener {
                        _isRegisterSuccess.value = true
                    }
                    .addOnFailureListener {
                        _errorMessage.value = "Gagal menyimpan data ke Firestore"
                    }
            }
            .addOnFailureListener {
                _errorMessage.value = it.message
            }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
