package com.imersa.warnu.ui.register.seller

import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.imersa.warnu.R
import com.imersa.warnu.databinding.ActivityRegisterSellerBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterSellerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterSellerBinding
    private val viewModel: RegisterSellerViewModel by viewModels()
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterSellerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white_login)
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

        // Toggle show/hide password
        binding.showPassword.setOnClickListener {
            isPasswordVisible = togglePasswordVisibility(
                binding.password,
                binding.showPassword,
                isPasswordVisible
            )
        }

        binding.showConfirmPassword.setOnClickListener {
            isConfirmPasswordVisible = togglePasswordVisibility(
                binding.confirmPassword,
                binding.showConfirmPassword,
                isConfirmPasswordVisible
            )
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }
}

private fun togglePasswordVisibility(
    editText: EditText,
    toggleIcon: ImageView,
    isVisible: Boolean
): Boolean {
    if (isVisible) {
        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        toggleIcon.setImageResource(R.drawable.ic_closed_eye)
    } else {
        editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        toggleIcon.setImageResource(R.drawable.ic_open_eye)
    }
    editText.typeface = Typeface.DEFAULT
    editText.setSelection(editText.text?.length ?: 0)
    return !isVisible
}

