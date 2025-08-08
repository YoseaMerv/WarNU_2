package com.imersa.warnu.ui.buyer

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import com.imersa.warnu.R

class MainBuyerActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_buyer)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        navigationView.menu.findItem(R.id.nav_manage_product).isVisible = false
        navigationView.menu.findItem(R.id.nav_order).isVisible = false
        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawer(GravityCompat.START)

            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    Toast.makeText(this, "Buka Profile", Toast.LENGTH_SHORT).show()
                    // TODO: ganti fragment atau start Activity profile
                }
                R.id.nav_history -> {
                    Toast.makeText(this, "Buka Riwayat Pembelian", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_favorites -> {
                    Toast.makeText(this, "Buka Favorit", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_logout -> {
                    Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
                    // TODO: clear session & pindah ke login
                }
            }
            true
        }
    }
}
