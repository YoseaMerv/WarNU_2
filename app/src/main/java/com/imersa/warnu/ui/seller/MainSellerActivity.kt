package com.imersa.warnu.ui.seller

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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
    private val viewModel: MainSellerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_seller)
        window.statusBarColor = ContextCompat.getColor(this, R.color.green)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.seller_toolbar)
        setSupportActionBar(toolbar)

        // Inisialisasi view
        drawerLayout = findViewById(R.id.drawer_layout_seller)
        navigationView = findViewById(R.id.navigation_view_seller)

        // Setup NavController dari NavHostFragment
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container_seller) as NavHostFragment
        navController = navHostFragment.navController

        // Setup AppBarConfiguration dengan drawer dan top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.HomeSellerFragment, R.id.EditManageFragment), // top level destinations
            drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        // Set header welcome user
        val headerView = navigationView.getHeaderView(0)
        val textViewWelcome = headerView.findViewById<TextView>(R.id.textViewNavHeader)

        viewModel.name.observe(this) { name ->
            textViewWelcome.text = "Selamat Datang, $name"
        }

        viewModel.userNotFound.observe(this) { notFound ->
            if (notFound) textViewWelcome.text = "Failed to load user"
        }

        viewModel.loadUserData()

        // Hide some menu items if needed
        navigationView.menu.findItem(R.id.nav_history).isVisible = false
        navigationView.menu.findItem(R.id.nav_favorites).isVisible = false

        // Handle menu drawer item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            val handled = when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Navigasi ke HomeSellerFragment
                    navController.navigate(R.id.HomeSellerFragment)
                    true
                }
                R.id.nav_manage_product -> {
                    // Navigasi ke EditManageFragment
                    navController.navigate(R.id.EditManageFragment)
                    true
                }
                R.id.nav_profile -> {
                    // Navigasi ke ProfileSellerFragment
                    navController.navigate(R.id.profileSellerFragment)
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

    // Handle tombol back, tutup drawer jika terbuka
//    override fun onBackPressed() {
//        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
//            drawerLayout.closeDrawer(GravityCompat.START)
//        } else {
//            super.onBackPressed()
//        }
//    }

    // Handle tombol Up di toolbar (drawer hamburger)
    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}
