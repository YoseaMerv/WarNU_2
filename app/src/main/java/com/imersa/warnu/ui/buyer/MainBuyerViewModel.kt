package com.imersa.warnu.ui.buyer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainBuyerViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _name = MutableLiveData<String>()
    val name: LiveData<String> = _name

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
                    _name.value = document.getString("name") ?: "User"
                } else {
                    _userNotFound.value = true
                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainBuyerViewModel", "Error fetching user data", exception)
                _userNotFound.value = true
            }
    }

    fun logout() {
        auth.signOut()
    }
}
