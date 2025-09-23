package com.imersa.warnu.ui.register.seller

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.regex.Pattern

class RegisterSellerViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _registerStatus = MutableLiveData<String>()
    val registerStatus: LiveData<String> get() = _registerStatus

    private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}" + "@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+"
    )

    fun register(
        name: String,
        email: String,
        phone: String,
        address: String,
        storeName: String,
        password: String,
        imageUri: Uri?
    ) {
        if (!isValidEmail(email)) {
            _registerStatus.value = "Error: Format email tidak valid."
            return
        }

        if (password.length < 6) {
            _registerStatus.value = "Error: Password minimal 6 karakter."
            return
        }

        _registerStatus.value = "Loading"
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId == null) {
                        _registerStatus.value = "Error: Gagal mendapatkan User ID."
                        return@addOnCompleteListener
                    }

                    if (imageUri != null) {
                        uploadImageAndSaveUser(
                            userId,
                            name,
                            email,
                            phone,
                            address,
                            storeName,
                            imageUri
                        )
                    } else {
                        saveUserToFirestore(userId, name, email, phone, address, storeName, "")
                    }
                } else {
                    _registerStatus.value = "Error: ${task.exception?.message}"
                }
            }
    }

    private fun isValidEmail(email: String): Boolean {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches()
    }


    private fun uploadImageAndSaveUser(
        userId: String,
        name: String,
        email: String,
        phone: String,
        address: String,
        storeName: String,
        imageUri: Uri
    ) {
        val storageRef = storage.reference.child("profile_pictures/${userId}_profile.jpg")

        storageRef.putFile(imageUri).addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveUserToFirestore(
                        userId,
                        name,
                        email,
                        phone,
                        address,
                        storeName,
                        downloadUrl.toString()
                    )
                }
            }.addOnFailureListener { e ->
                _registerStatus.value = "Error: Gagal mengunggah gambar - ${e.message}"
            }
    }

    private fun saveUserToFirestore(
        userId: String,
        name: String,
        email: String,
        phone: String,
        address: String,
        storeName: String,
        photoUrl: String
    ) {
        val user = hashMapOf(
            "uid" to userId,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "address" to address,
            "storeName" to storeName,
            "role" to "seller",
            "photoUrl" to photoUrl
        )

        firestore.collection("users").document(userId).set(user).addOnSuccessListener {
                _registerStatus.value = "Success"
            }.addOnFailureListener { e ->
                _registerStatus.value = "Error: Gagal menyimpan data ke Firestore - ${e.message}"
            }
    }
}