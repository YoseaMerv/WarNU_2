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
class MainSellerViewModel @Inject constructor() : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> = _fullName

    private val _userNotFound = MutableLiveData<Boolean>()
    val userNotFound: LiveData<Boolean> = _userNotFound

    fun loadUserData() {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("fullName") ?: "Seller"
                        _fullName.value = name
                    } else {
                        _userNotFound.value = true
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainSellerViewModel", "Error fetching user data", e)
                    _userNotFound.value = true
                }
        } else {
            _userNotFound.value = true
        }
    }

    fun logout() {
        auth.signOut()
    }
}
