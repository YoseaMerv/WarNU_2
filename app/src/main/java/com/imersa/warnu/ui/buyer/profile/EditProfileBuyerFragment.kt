package com.imersa.warnu.ui.buyer.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentEditProfileBuyerBinding

class EditProfileBuyerFragment : Fragment() {

    private var _binding: FragmentEditProfileBuyerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProfileBuyerViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfilePicture.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBuyerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadUserProfile()
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.cardProfileImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(requireContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateUserProfile(name, phone, address, selectedImageUri)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            binding.etName.setText(user.name)
            binding.etPhone.setText(user.phone)
            binding.etAddress.setText(user.address)
            Glide.with(this)
                .load(user.photoUrl)
                .placeholder(R.drawable.ic_user)
                .circleCrop()
                .into(binding.ivProfilePicture)
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { status ->
            when {
                status.startsWith("Loading") -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                status.startsWith("Success") -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp() // Kembali ke halaman profil
                }
                status.startsWith("Error") -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), status, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}