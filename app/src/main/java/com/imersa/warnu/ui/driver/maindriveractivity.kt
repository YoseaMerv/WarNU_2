package com.imersa.warnu.ui.driver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.imersa.warnu.R

class MainDriverActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_driver)

        // --- Kode untuk Sidebar ---
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout_main)
        setSupportActionBar(toolbar)
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Set ikon hamburger langsung di sini agar konsisten
        supportActionBar?.setHomeAsUpIndicator(R.drawable.hamburger_menu)

        // --- Kode untuk Peta ---
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_container) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val surabaya = LatLng(-7.2575, 112.7521)
        mMap.addMarker(MarkerOptions().position(surabaya).title("Penanda di Surabaya"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(surabaya, 15f))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
