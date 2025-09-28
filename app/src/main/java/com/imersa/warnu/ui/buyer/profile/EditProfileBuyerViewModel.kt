package com.imersa.warnu.ui.buyer.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EditProfileBuyerViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    private val userId = auth.currentUser?.uid

    private val _updateStatus = MutableLiveData<String>()
    val updateStatus: LiveData<String> get() = _updateStatus

    private val _userData = MutableLiveData<Map<String, Any>>()
    val userData: LiveData<Map<String, Any>> get() = _userData

    private var oldPhotoUrl: String? = null

    fun fetchUserData() {
        if (userId == null) {
            _updateStatus.value = "Error: User not found."
            return
        }
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val data = document.data ?: emptyMap()
                    _userData.value = data
                    // Gunakan "profileImageUrl" sesuai dengan standar baru kita
                    oldPhotoUrl = data["profileImageUrl"] as? String
                } else {
                    _updateStatus.value = "Error: User data not found."
                }
            }
            .addOnFailureListener { e ->
                _updateStatus.value = "Error: Failed to fetch data - ${e.message}"
            }
    }

    fun updateUser(
        context: Context, name: String, phone: String, address: String, imageUri: Uri?
    ) {
        if (userId == null) {
            _updateStatus.value = "Error: User not found."
            return
        }

        _updateStatus.value = "Loading"

        if (imageUri != null) {
            // Validasi file gambar sederhana
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val fileSize = inputStream?.available() ?: 0
                inputStream?.close()

                if (fileSize <= 0) {
                    _updateStatus.value = "Error: Invalid image file."
                    return
                }
            } catch (e: Exception) {
                _updateStatus.value = "Error: Cannot read file - ${e.message}"
                return
            }

            uploadImageAndUpdateUser(userId, name, phone, address, imageUri)
        } else {
            updateUserData(userId, name, phone, address, null)
        }
    }

    private fun uploadImageAndUpdateUser(
        userId: String, name: String, phone: String, address: String, imageUri: Uri
    ) {
        val storageRef =
            storage.reference.child("profile_pictures/${userId}_${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    updateUserData(userId, name, phone, address, downloadUrl.toString())

                    oldPhotoUrl?.let { url ->
                        if (url.isNotEmpty()) {
                            deleteOldPhoto(url)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                _updateStatus.value = "Error: Failed to upload image - ${e.message}"
            }
    }

    private fun updateUserData(
        userId: String, name: String, phone: String, address: String, photoUrl: String?
    ) {
        val userUpdates = mutableMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "address" to address
        )

        if (photoUrl != null) {
            // Gunakan "profileImageUrl" agar konsisten
            userUpdates["profileImageUrl"] = photoUrl
        }

        firestore.collection("users").document(userId).update(userUpdates)
            .addOnSuccessListener {
                _updateStatus.value = "Success"
            }
            .addOnFailureListener { e ->
                _updateStatus.value = "Error: Failed to update data - ${e.message}"
            }
    }

    private fun deleteOldPhoto(url: String) {
        try {
            val oldRef = storage.getReferenceFromUrl(url)
            oldRef.delete()
        } catch (e: Exception) {
        }
    }
}