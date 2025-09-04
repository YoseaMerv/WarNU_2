package com.imersa.warnu.data.model

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val storeName: String? = null, // storeName bisa null untuk buyer
    // ✅ TAMBAHKAN DUA BARIS INI
    val address: String = "",
    val photoUrl: String = ""
)