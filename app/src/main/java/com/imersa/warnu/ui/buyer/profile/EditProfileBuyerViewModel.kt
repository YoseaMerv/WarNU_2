package com.imersa.warnu.ui.buyer.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.imersa.warnu.data.model.UserProfile

class EditProfileBuyerViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> get() = _userProfile

    private val _updateStatus = MutableLiveData<String>()
    val updateStatus: LiveData<String> get() = _updateStatus

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun loadUserProfile() {
        _isLoading.value = true
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(UserProfile::class.java)
                user?.let {
                    _userProfile.value = it
                }
            }
            .addOnFailureListener {
                // Handle error
            }
            .addOnCompleteListener {
                _isLoading.value = false
            }
    }

    fun updateUserProfile(name: String, phone: String, address: String, newImageUri: Uri?) {
        _updateStatus.value = "Loading"
        val userId = auth.currentUser?.uid ?: return

        if (newImageUri != null) {
            uploadImageAndUpdateProfile(userId, name, phone, address, newImageUri)
        } else {
            updateProfileInFirestore(userId, name, phone, address, _userProfile.value?.photoUrl ?: "")
        }
    }

    private fun uploadImageAndUpdateProfile(userId: String, name: String, phone: String, address: String, imageUri: Uri) {
        val storageRef = storage.reference.child("profile_pictures/${userId}_profile.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    updateProfileInFirestore(userId, name, phone, address, downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                _updateStatus.value = "Error: ${e.message}"
            }
    }

    private fun updateProfileInFirestore(userId: String, name: String, phone: String, address: String, photoUrl: String) {
        val updates = mapOf(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "photoUrl" to photoUrl
        )

        firestore.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                _updateStatus.value = "Success"
            }
            .addOnFailureListener { e ->
                _updateStatus.value = "Error: ${e.message}"
            }
    }
}