package com.imersa.warnu.ui.seller

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainSellerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> = _fullName

    private val _userNotFound = MutableLiveData<Boolean>()
    val userNotFound: LiveData<Boolean> = _userNotFound

    fun loadUserData() {
        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            _userNotFound.value = true
            return
        }

        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    _fullName.value = document.getString("fullName") ?: "Seller"
                } else {
                    _userNotFound.value = true
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainSellerViewModel", "Error fetching user data", exception)
                _userNotFound.value = true
            }
    }

    fun logout() {
        auth.signOut()
    }
}
