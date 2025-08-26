package com.imersa.warnu.ui.seller.main

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

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name
    private val _email = MutableLiveData<String>()
    val email: LiveData<String> = _email
    private val _phone = MutableLiveData<String>()
    val phone: LiveData<String> = _phone
    private val _address = MutableLiveData<String>()
    val address: LiveData<String> = _address
    private val _role = MutableLiveData<String>()
    val role: LiveData<String> = _role
    private val _uid = MutableLiveData<String>()
    val uid: LiveData<String> = _uid
    private val _storeName = MutableLiveData<String>()
    val storeName: LiveData<String> = _storeName

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
                    _name.value = document.getString("name") ?: _role.value
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
