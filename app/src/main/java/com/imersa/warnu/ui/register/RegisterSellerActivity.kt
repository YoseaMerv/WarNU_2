package com.imersa.warnu.ui.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.imersa.warnu.databinding.ActivityRegisterSellerBinding
import com.imersa.warnu.ui.register.RegisterSellerViewModel


class RegisterSellerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterSellerBinding
    private val viewModel: RegisterSellerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterSellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        viewModel.registerResult.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke login
            } else {
                Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.registerButton.setOnClickListener {
            val name = binding.fullName.text.toString().trim()
            val storeName = binding.etStoreName.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhoneNumber.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (name.isEmpty() || storeName.isEmpty() || address.isEmpty()
                || email.isEmpty() || phone.isEmpty() || password.isEmpty()
            ) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.registerSeller(name, storeName, address, email, phone, password)
            }
        }

        binding.tvLogin.setOnClickListener {
            finish() // Kembali ke login
        }
    }
}


