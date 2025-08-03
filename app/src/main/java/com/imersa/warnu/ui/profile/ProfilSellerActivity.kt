package com.imersa.warnu.ui.profile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.imersa.warnu.R

class ProfilSellerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile_seller)

        // Mengisi data langsung ke setiap TextView dengan ID unik
        findViewById<TextView>(R.id.tvNamaValue).text = "Aldi Seller"
        findViewById<TextView>(R.id.tvEmailValue).text = "aldiseller@gmail.com"
        findViewById<TextView>(R.id.tvTeleponValue).text = "XXXXXXXXXX"
        findViewById<TextView>(R.id.tvAlamatValue).text = "XXXXXXXXXX"
        findViewById<TextView>(R.id.tvRoleValue).text = "Aldixxxxxx"


        // Kode bawaan untuk mengatur padding system bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
