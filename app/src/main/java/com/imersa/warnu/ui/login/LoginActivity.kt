package com.imersa.warnu.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.imersa.warnu.databinding.ActivityLoginBinding
import com.imersa.warnu.ui.buyer.MainBuyerActivity
import com.imersa.warnu.ui.seller.MainSellerActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
