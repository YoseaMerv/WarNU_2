package com.imersa.warnu.ui.buyer

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
class MainBuyerActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var btnAksiBuyer: MaterialButton

    private val viewModel: MainBuyerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_buyer)

        // Setup Toolbar
        val toolbar = findViewById<Toolbar>(R.id.buyer_toolbar)
        setSupportActionBar(toolbar)

        // Init Views
        drawerLayout = findViewById(R.id.drawer_layout_buyer)
        navigationView = findViewById(R.id.navigation_view_buyer)
        btnAksiBuyer = findViewById(R.id.btnAksiBuyer)

        // Setup NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_container_buyer) as NavHostFragment
        navController = navHostFragment.navController

        // Pastikan ini pakai ID dari nav_graph_buyer.xml
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeBuyerFragment,       // <- ID startDestination
            ),
            drawerLayout
        )

        // Hubungkan Toolbar dan NavigationView
        setupActionBarWithNavController(navController, appBarConfiguration)
        navigationView.setupWithNavController(navController)

        // Atur tombol aksi hanya di halaman Home
        navController.addOnDestinationChangedListener { _, destination, _ ->
            btnAksiBuyer.visibility =
                if (destination.id == R.id.nav_home) View.VISIBLE else View.GONE
        }

        btnAksiBuyer.setOnClickListener {
            Toast.makeText(this, "Aksi buyer dijalankan", Toast.LENGTH_SHORT).show()
        }

        // Header Navigation Drawer
        navigationView.getHeaderView(0)?.let { headerView ->
            val textViewWelcome = headerView.findViewById<TextView>(R.id.textViewNavHeader)
            viewModel.name.observe(this) { name ->
                textViewWelcome.text = "Selamat Datang, $name"
            }
            viewModel.userNotFound.observe(this) { notFound ->
                if (notFound) textViewWelcome.text = "Failed to load user"
            }
        }

        // Load data user
        viewModel.loadUserData()

        // Nonaktifkan menu yang tidak perlu
        navigationView.menu.findItem(R.id.nav_manage_product)?.isVisible = false
        navigationView.menu.findItem(R.id.nav_order)?.isVisible = false

        // Custom klik menu drawer untuk yang tidak di-handle default navigation
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    viewModel.logout()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> {
                    val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                    if (handled) drawerLayout.closeDrawer(GravityCompat.START)
                    handled
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
