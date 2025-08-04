package com.imersa.warnu.ui.seller

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.imersa.warnu.R
import com.imersa.warnu.ui.login.LoginActivity
import com.imersa.warnu.ui.seller.product.ManageProductFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainSellerActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    private val viewModel: MainSellerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.hide()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_main_seller)

        drawerLayout = findViewById(R.id.drawer_layout_seller)
        navigationView = findViewById(R.id.navigation_view_seller)
        val hamburgerIcon = findViewById<ImageView>(R.id.hamburger_icon_seller)
        val textViewWelcome = findViewById<TextView>(R.id.textViewSeller)

        // Drawer open
        hamburgerIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Observasi nama user
        viewModel.fullName.observe(this) { name ->
            textViewWelcome.text = "Welcome, $name"
        }

        viewModel.userNotFound.observe(this) { notFound ->
            if (notFound) textViewWelcome.text = "Failed to load user"
        }

        viewModel.loadUserData()

        // Drawer menu klik
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profil -> {
                    Toast.makeText(this, "Profil Saya diklik", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_kelola_produk -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_seller, ManageProductFragment())
                        .commit()
                    true
                }
                R.id.nav_pesanan_masuk -> {
                    Toast.makeText(this, "Pesanan Masuk diklik", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_logout -> {
                    viewModel.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }.also {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        // Tampilkan default fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_seller, ManageProductFragment())
                .commit()
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
