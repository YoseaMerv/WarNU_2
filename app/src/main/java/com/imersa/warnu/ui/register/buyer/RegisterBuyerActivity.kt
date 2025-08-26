package com.imersa.warnu.ui.register.buyer

import android.content.Intent
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
import com.imersa.warnu.databinding.ActivityRegisterBuyerBinding
import com.imersa.warnu.ui.login.LoginActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterBuyerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBuyerBinding
    private val viewModel: RegisterBuyerViewModel by viewModels()
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBuyerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white_login)
        supportActionBar?.hide()

        // Register logic
        binding.registerButton.setOnClickListener {
            val name = binding.fullName.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val phone = binding.phoneNumber.text.toString().trim()
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            if (password != confirmPassword) {
                Toast.makeText(this, "Password tidak sama", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.registerBuyer(name, address, email, phone, password)
        }

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

        binding.loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        viewModel.isRegisterSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Registrasi berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                viewModel.clearError()
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
}
