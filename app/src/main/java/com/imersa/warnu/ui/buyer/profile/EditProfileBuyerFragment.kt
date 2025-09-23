package com.imersa.warnu.ui.buyer.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.imersa.warnu.databinding.FragmentEditProfileBuyerBinding

class EditProfileBuyerFragment : Fragment() {

    private var _binding: FragmentEditProfileBuyerBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EditProfileBuyerViewModel
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivProfile.setImageURI(selectedImageUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditProfileBuyerBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this)[EditProfileBuyerViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = "Edit Profil"

        setupObservers()
        setupClickListeners()

        viewModel.fetchUserData()
    }

    private fun setupClickListeners() {
        binding.btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(requireContext(), "Harap isi semua kolom", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateUser(requireContext(), name, phone, address, selectedImageUri)
        }
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            binding.etName.setText(user["name"] as? String)
            binding.etPhone.setText(user["phone"] as? String)
            binding.etAddress.setText(user["address"] as? String)

            val photoUrl = user["photourl"] as? String
            if (!photoUrl.isNullOrEmpty()) {
                Glide.with(this).load(photoUrl).circleCrop().into(binding.ivProfile)
            }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { status ->
            when {
                status.startsWith("Loading") -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }

                status.startsWith("Success") -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        requireContext(),
                        "Profil berhasil diperbarui",
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }

                status.startsWith("Error:") -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    val errorMessage = status.substringAfter("Error: ")
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}