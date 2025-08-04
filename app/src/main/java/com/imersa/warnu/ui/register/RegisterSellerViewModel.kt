package com.imersa.warnu.ui.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class RegisterSellerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _registerResult = MutableLiveData<Boolean>()
    val registerResult: LiveData<Boolean> = _registerResult

    fun registerSeller(
        name: String,
        storeName: String,
        address: String,
        email: String,
        phone: String,
        password: String
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val sellerData = hashMapOf(
                        "uid" to userId,
                        "name" to name,
                        "storeName" to storeName,
                        "address" to address,
                        "email" to email,
                        "phone" to phone,
                        "role" to "seller"
                    )
                    firestore.collection("users").document(userId)
                        .set(sellerData)
                        .addOnSuccessListener {
                            _registerResult.value = true
                        }
                        .addOnFailureListener {
                            _registerResult.value = false
                        }
                } else {
                    _registerResult.value = false
                }
            }
    }
}
