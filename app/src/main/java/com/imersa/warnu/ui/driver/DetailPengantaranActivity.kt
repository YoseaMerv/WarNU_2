package com.imersa.warnu.ui.driver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.imersa.warnu.R

class DetailPengantaranActivity : AppCompatActivity() {

    // Deklarasikan variabel untuk sidebar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Hubungkan file Kotlin ini ke layout XML-nya
        setContentView(R.layout.activity_detail_pengantaran)

        // --- Kode untuk membuat Sidebar berfungsi ---

        // 2. Inisialisasi semua komponen dari layout XML
        val toolbar: Toolbar = findViewById(R.id.toolbar_detail)
        drawerLayout = findViewById(R.id.drawer_layout_detail)

        // 3. Jadikan Toolbar sebagai ActionBar
        setSupportActionBar(toolbar)

        // 4. Buat objek "toggle" yang akan menampilkan ikon hamburger
        // dan mengontrol buka-tutup sidebar saat ikon ditekan.
        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar, // Hubungkan ke toolbar
            R.string.navigation_drawer_open, // Teks untuk aksesibilitas
            R.string.navigation_drawer_close // Teks untuk aksesibilitas
        )

        // 5. Pasang "toggle" ke sidebar dan sinkronkan statusnya
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // 6. Tampilkan tombol "Up" (ikon hamburger) di ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    // 7. Fungsi ini WAJIB ada agar klik pada ikon hamburger bisa ditangani
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Jika toggle (ikon hamburger) yang diklik, maka fungsi ini akan ditangani
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
