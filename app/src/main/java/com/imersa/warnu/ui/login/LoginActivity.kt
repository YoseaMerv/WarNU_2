package com.imersa.warnu.ui.login

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.imersa.warnu.databinding.ActivityLoginBinding
import com.imersa.warnu.ui.buyer.main.MainBuyerActivity
import com.imersa.warnu.ui.seller.main.MainSellerActivity
import dagger.hilt.android.AndroidEntryPoint
import android.text.InputType
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.imersa.warnu.R
import com.imersa.warnu.ui.register.buyer.RegisterBuyerActivity
import com.imersa.warnu.ui.register.seller.RegisterSellerActivity


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white_login)
        setContentView(binding.root)

        setupListeners()
        observeLoginState()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailInput.text.toString().trim()
            val password = binding.password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan Password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        binding.showPassword.setOnClickListener {
            isPasswordVisible = togglePasswordVisibility()
        }

        binding.signUpBuyer.setOnClickListener {
            startActivity(Intent(this, RegisterBuyerActivity::class.java))
        }

        binding.signUpSeller.setOnClickListener {
            startActivity(Intent(this, RegisterSellerActivity::class.java))
        }

    }

    private fun togglePasswordVisibility(): Boolean {
        if (isPasswordVisible) {
            binding.password.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.showPassword.setImageResource(R.drawable.ic_closed_eye)
        } else {
            binding.password.inputType =
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.showPassword.setImageResource(R.drawable.ic_open_eye)
        }

        binding.password.typeface = Typeface.DEFAULT
        binding.password.setSelection(binding.password.text?.length ?: 0)
        return !isPasswordVisible
    }

    private fun observeLoginState() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.loginProgressBar.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false
                }
                is LoginState.Success -> {
                    binding.loginProgressBar.visibility = View.GONE
                    navigateToHome(state.role)
                }
                is LoginState.Error -> {
                    binding.loginProgressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToHome(role: String) {
        val intent = when (role) {
            "seller" -> Intent(this, MainSellerActivity::class.java)
            "buyer" -> Intent(this, MainBuyerActivity::class.java)
            else -> {
                Toast.makeText(this, "Role tidak valid: $role", Toast.LENGTH_SHORT).show()
                return
            }
        }
        startActivity(intent)
        finish()
    }
}
