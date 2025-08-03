package com.imersa.warnu.ui.seller

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.R

import com.imersa.warnu.ui.login.LoginActivity

class MainSellerActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_main_seller)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // View Sidebar
        drawerLayout = findViewById(R.id.drawer_layout_seller)
        navigationView = findViewById(R.id.navigation_view_seller)
        val hamburgerIcon = findViewById<ImageView>(R.id.hamburger_icon_seller)

        // View header
        val textViewWelcome = findViewById<TextView>(R.id.textViewSeller)

        // Buka drawer saat hamburger diklik
        hamburgerIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Ambil nama dari Firestore
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("fullName") ?: "Seller"
                        textViewWelcome.text = "Welcome, $fullName"
                    } else {
                        textViewWelcome.text = "User data not found."
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("MainSellerActivity", "Error getting user data", e)
                    textViewWelcome.text = "Failed to load data."
                }
        } else {
            textViewWelcome.text = "Not logged in."
        }

        // Tangani klik menu navigasi sidebar
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profil -> {
                    Toast.makeText(this, "Profil Saya diklik", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_kelola_produk -> {
                    Toast.makeText(this, "Kelola Produk diklik", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_pesanan_masuk -> {
                    Toast.makeText(this, "Pesanan Masuk diklik", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_logout -> {
                    auth.signOut()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

//    override fun onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START)
//        } else {
//            super.onBackPressed()
//        }
//    }
}
