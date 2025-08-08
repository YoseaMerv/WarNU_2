package com.imersa.warnu.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.imersa.warnu.R
import com.imersa.warnu.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainSellerActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var btnTambahProduk: MaterialButton

    private val viewModel: MainSellerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_seller)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.seller_toolbar)
        setSupportActionBar(toolbar)

        // Inisialisasi view
        drawerLayout = findViewById(R.id.drawer_layout_seller)
        navigationView = findViewById(R.id.navigation_view_seller)
        btnTambahProduk = findViewById(R.id.btnTambahProdukSeller)

        // Setup NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container_seller) as NavHostFragment
        navController = navHostFragment.navController

        // Setup AppBarConfiguration & Toolbar
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.dataSellerFragment),
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        // Tampilkan tombol tambah produk hanya di DataProductFragment
        navController.addOnDestinationChangedListener { _, destination, _ ->
            btnTambahProduk.visibility =
                if (destination.id == R.id.dataSellerFragment) View.VISIBLE else View.GONE
        }

        // Klik tombol tambah produk
        btnTambahProduk.setOnClickListener {
            navController.navigate(R.id.addProductFragment)
        }

        // Set header "Welcome"
        val headerView = navigationView.getHeaderView(0)
        val textViewWelcome = headerView.findViewById<TextView>(R.id.textViewSeller)

        viewModel.fullName.observe(this) { name ->
            textViewWelcome.text = "Welcome, $name"
        }

        viewModel.userNotFound.observe(this) { notFound ->
            if (notFound) textViewWelcome.text = "Failed to load user"
        }

        viewModel.loadUserData()

        // Navigasi menu drawer
        navigationView.setNavigationItemSelectedListener { menuItem ->
            val handled = when (menuItem.itemId) {
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profil Saya diklik", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_manage_product -> {
                    Toast.makeText(this, "Kelola Pesanan diklik", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_order -> {
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
            }
            if (handled) drawerLayout.closeDrawer(GravityCompat.START)
            handled
        }
    }

//    // Handle tombol back
//    override fun onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START)
//        } else {
//            super.onBackPressed()
//        }
//    }

    // Handle tombol "up" di AppBar
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
