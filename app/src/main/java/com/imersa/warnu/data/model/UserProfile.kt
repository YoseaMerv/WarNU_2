package com.imersa.warnu.data.model

data class UserProfile(
    val name: String? = null,
    val email: String? = null,
    val phone: String? = null
    // Tambahkan field lain jika ada, misal: address
)