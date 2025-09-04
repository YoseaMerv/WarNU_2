package com.imersa.warnu.ui.register.buyer

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RegisterBuyerViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _registerStatus = MutableLiveData<String>()
    val registerStatus: LiveData<String> get() = _registerStatus

    // **ALUR BARU PADA FUNGSI UTAMA**
    fun register(
        name: String, email: String, phone: String, address: String, password: String, imageUri: Uri?
    ) {
        _registerStatus.value = "Loading"
        // 1. Buat user di Firebase Auth terlebih dahulu
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId == null) {
                        _registerStatus.value = "Error: Gagal mendapatkan User ID."
                        return@addOnCompleteListener
                    }

                    // 2. Jika ada gambar, unggah. Jika tidak, langsung simpan.
                    if (imageUri != null) {
                        uploadImageAndSaveUser(userId, name, email, phone, address, imageUri)
                    } else {
                        saveUserToFirestore(userId, name, email, phone, address, "")
                    }
                } else {
                    _registerStatus.value = "Error: ${task.exception?.message}"
                }
            }
    }

    private fun uploadImageAndSaveUser(
        userId: String, name: String, email: String, phone: String, address: String, imageUri: Uri
    ) {
        val storageRef = storage.reference.child("profile_pictures/${userId}_profile.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // 3. Simpan semua data ke Firestore
                    saveUserToFirestore(userId, name, email, phone, address, downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                _registerStatus.value = "Error: Gagal mengunggah gambar - ${e.message}"
            }
    }

    private fun saveUserToFirestore(
        userId: String, name: String, email: String, phone: String, address: String, photoUrl: String
    ) {
        val user = hashMapOf(
            "uid" to userId,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "address" to address,  // Pastikan field ini benar
            "role" to "buyer",
            "photoUrl" to photoUrl // Pastikan field ini benar
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                _registerStatus.value = "Success"
            }
            .addOnFailureListener { e ->
                _registerStatus.value = "Error: Gagal menyimpan data - ${e.message}"
            }
    }
}