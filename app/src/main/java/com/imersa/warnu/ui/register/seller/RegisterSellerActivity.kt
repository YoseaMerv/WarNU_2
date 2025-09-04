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

    // Variabel untuk menyimpan URI gambar yang dipilih
    private var selectedImageUri: Uri? = null

    // Launcher untuk membuka galeri gambar
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            // Tampilkan gambar yang dipilih di ImageView
            binding.ivProfilePicture.setImageURI(it)
            binding.ivProfilePicture.setPadding(0, 0, 0, 0) // Hapus padding jika ada
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
        // Tambahkan listener klik pada ImageView untuk memilih gambar
        binding.cardProfileImage.setOnClickListener {
            imagePickerLauncher.launch("image/*") // Buka galeri
        }
        binding.ivProfilePicture.setOnClickListener {
            imagePickerLauncher.launch("image/*") // Buka galeri
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

            // Panggil fungsi register di ViewModel dengan menyertakan URI gambar
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
                    // Arahkan ke halaman login
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