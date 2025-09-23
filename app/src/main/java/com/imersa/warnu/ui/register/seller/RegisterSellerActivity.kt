package com.imersa.warnu.ui.register.seller

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.imersa.warnu.databinding.ActivityRegisterSellerBinding
import com.imersa.warnu.ui.login.LoginActivity

class RegisterSellerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterSellerBinding
    private val viewModel: RegisterSellerViewModel by viewModels()

    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfilePicture.setImageURI(it)
            binding.ivProfilePicture.setPadding(0, 0, 0, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterSellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.cardProfileImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        binding.ivProfilePicture.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val storeName = binding.etStoreName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (name.isEmpty() || storeName.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Semua kolom wajib diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.register(name, email, phone, address, storeName, password, selectedImageUri)
        }
    }

    private fun observeViewModel() {
        viewModel.registerStatus.observe(this) { status ->
            when {
                status.startsWith("Loading") -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRegister.isEnabled = false
                }

                status.startsWith("Success") -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }

                status.startsWith("Error") -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    Toast.makeText(this, status, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}