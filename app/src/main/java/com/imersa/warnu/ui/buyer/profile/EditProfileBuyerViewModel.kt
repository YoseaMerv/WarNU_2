package com.imersa.warnu.ui.buyer.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileBuyerViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val userId = auth.currentUser?.uid

    private val _updateStatus = MutableLiveData<String>()
    val updateStatus: LiveData<String> get() = _updateStatus

    private val _userData = MutableLiveData<Map<String, Any>>()
    val userData: LiveData<Map<String, Any>> get() = _userData

    // 🔹 Ambil data pengguna
    fun fetchUserData() {
        if (userId == null) {
            _updateStatus.value = "Error: User tidak ditemukan."
            return
        }
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    _userData.value = document.data
                } else {
                    _updateStatus.value = "Error: Data pengguna tidak ditemukan."
                }
            }
            .addOnFailureListener { e ->
                _updateStatus.value = "Error: Gagal mengambil data - ${e.message}"
            }
    }

    // 🔹 Update profil pengguna
    fun updateUser(
        context: Context,
        name: String,
        phone: String,
        address: String,
        imageUri: Uri?
    ) {
        if (userId == null) {
            _updateStatus.value = "Error: User tidak ditemukan."
            return
        }

        _updateStatus.value = "Loading"

        if (imageUri != null) {
            // Cek apakah file valid sebelum upload
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                val fileSize = inputStream?.available() ?: 0
                inputStream?.close()

                if (fileSize <= 0) {
                    _updateStatus.value = "Error: File gambar tidak valid."
                    return
                }
            } catch (e: Exception) {
                _updateStatus.value = "Error: Tidak bisa membaca file - ${e.message}"
                return
            }

            // Upload fresh (tanpa resume session lama)
            uploadImageAndUpdateUser(userId, name, phone, address, imageUri)
        } else {
            // Kalau tidak ada foto baru, update data teks saja
            updateUserData(userId, name, phone, address, null)
        }
    }

    // 🔹 Upload foto baru + update Firestore
    private fun uploadImageAndUpdateUser(
        userId: String,
        name: String,
        phone: String,
        address: String,
        imageUri: Uri
    ) {
        // Nama file unik → biar session upload selalu fresh
        val storageRef = storage.reference.child("profile_pictures/${userId}_${System.currentTimeMillis()}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    updateUserData(userId, name, phone, address, downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                _updateStatus.value = "Error: Gagal mengunggah gambar - ${e.message}"
            }
    }

    // 🔹 Update data ke Firestore
    private fun updateUserData(
        userId: String,
        name: String,
        phone: String,
        address: String,
        photoUrl: String?
    ) {
        val userUpdates = mutableMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "address" to address
        )

        // Firestore pakai field "photourl" (bukan photoUrl camelCase)
        if (photoUrl != null) {
            userUpdates["photourl"] = photoUrl
        }

        db.collection("users").document(userId)
            .update(userUpdates)
            .addOnSuccessListener {
                _updateStatus.value = "Success"
            }
            .addOnFailureListener { e ->
                _updateStatus.value = "Error: Gagal memperbarui data - ${e.message}"
            }
    }
}
