package com.imersa.warnu.ui.buyer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.imersa.warnu.databinding.ActivityMainBuyerBinding

class MainBuyerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBuyerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBuyerBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
}
