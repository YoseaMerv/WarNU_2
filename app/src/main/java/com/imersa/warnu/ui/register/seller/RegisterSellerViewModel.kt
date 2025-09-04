package com.imersa.warnu.ui.register.seller

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class RegisterSellerViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val _registerStatus = MutableLiveData<String>()
    val registerStatus: LiveData<String> get() = _registerStatus

    // **ALUR BARU PADA FUNGSI UTAMA**
    fun register(
        name: String, email: String, phone: String, address: String, storeName: String, password: String, imageUri: Uri?
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

                    // 2. Jika ada gambar, unggah gambar. Jika tidak, langsung simpan data.
                    if (imageUri != null) {
                        uploadImageAndSaveUser(userId, name, email, phone, address, storeName, imageUri)
                    } else {
                        saveUserToFirestore(userId, name, email, phone, address, storeName, "")
                    }
                } else {
                    _registerStatus.value = "Error: ${task.exception?.message}"
                }
            }
    }

    // Fungsi untuk mengunggah gambar SETELAH user dibuat
    private fun uploadImageAndSaveUser(
        userId: String, name: String, email: String, phone: String, address: String, storeName: String, imageUri: Uri
    ) {
        // Gunakan UID user untuk path gambar agar unik dan aman
        val storageRef = storage.reference.child("profile_pictures/${userId}_profile.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // 3. Setelah URL didapat, simpan semua data ke Firestore
                    saveUserToFirestore(userId, name, email, phone, address, storeName, downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                _registerStatus.value = "Error: Gagal mengunggah gambar - ${e.message}"
            }
    }

    // Fungsi untuk menyimpan data ke Firestore
    private fun saveUserToFirestore(
        userId: String, name: String, email: String, phone: String, address: String, storeName: String, photoUrl: String
    ) {
        val user = hashMapOf(
            "uid" to userId,
            "name" to name,
            "email" to email,
            "phone" to phone,
            "address" to address,      // Pastikan field ini benar
            "storeName" to storeName,
            "role" to "seller",
            "photoUrl" to photoUrl // Pastikan field ini benar
        )

        firestore.collection("users").document(userId)
            .set(user)
            .addOnSuccessListener {
                _registerStatus.value = "Success"
            }
            .addOnFailureListener { e ->
                _registerStatus.value = "Error: Gagal menyimpan data ke Firestore - ${e.message}"
            }
    }
}