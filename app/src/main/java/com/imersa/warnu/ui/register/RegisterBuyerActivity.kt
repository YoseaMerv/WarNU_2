package com.imersa.warnu.ui.register

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.imersa.warnu.databinding.ActivityRegisterBuyerBinding
import com.imersa.warnu.ui.register.RegisterBuyerViewModel

class RegisterBuyerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBuyerBinding
    private val viewModel: RegisterBuyerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBuyerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.registerButton.setOnClickListener {
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.registerBuyer(email, password)
            }
        }

        viewModel.registerStatus.observe(this, Observer { isSuccess ->
            if (isSuccess) {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                // TODO: Arahkan ke halaman utama buyer atau login
            } else {
                Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show()
            }
        })

        binding.loginLink.setOnClickListener {
            finish()
        }
    }
}
