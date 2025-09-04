package com.imersa.warnu.ui.seller.profile

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditProfileSellerViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val userId = auth.currentUser?.uid

    private val _updateStatus = MutableLiveData<String>()
    val updateStatus: LiveData<String> get() = _updateStatus

    private val _userData = MutableLiveData<Map<String, Any>>()
    val userData: LiveData<Map<String, Any>> get() = _userData

    // Fungsi untuk mengambil data pengguna saat ini
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

    // Fungsi untuk memperbarui profil pengguna
    fun updateUser(
        name: String,
        phone: String,
        address: String,
        storeName: String,
        imageUri: Uri?
    ) {
        if (userId == null) {
            _updateStatus.value = "Error: User tidak ditemukan."
            return
        }

        _updateStatus.value = "Loading"

        if (imageUri != null) {
            // Jika ada gambar baru, unggah dulu
            uploadImageAndUpdateUser(userId, name, phone, address, storeName, imageUri)
        } else {
            // Jika tidak ada gambar baru, langsung update data teks
            updateUserData(userId, name, phone, address, storeName, null)
        }
    }

    private fun uploadImageAndUpdateUser(
        userId: String, name: String, phone: String, address: String, storeName: String, imageUri: Uri
    ) {
        val storageRef = storage.reference.child("profile_pictures/${userId}_profile.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Setelah gambar berhasil diunggah, perbarui data dengan URL gambar baru
                    updateUserData(userId, name, phone, address, storeName, downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                _updateStatus.value = "Error: Gagal mengunggah gambar - ${e.message}"
            }
    }

    private fun updateUserData(
        userId: String, name: String, phone: String, address: String, storeName: String, photoUrl: String?
    ) {
        val userUpdates = mutableMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "storeName" to storeName
        )

        // Hanya tambahkan photoUrl ke dalam map jika ada URL gambar yang baru
        if (photoUrl != null) {
            userUpdates["photoUrl"] = photoUrl
        }

        db.collection("users").document(userId)
            .update(userUpdates) // 🔥 Menggunakan .update() untuk memperbarui field yang ada
            .addOnSuccessListener {
                _updateStatus.value = "Success"
            }
            .addOnFailureListener { e ->
                _updateStatus.value = "Error: Gagal memperbarui data - ${e.message}"
            }
    }
}