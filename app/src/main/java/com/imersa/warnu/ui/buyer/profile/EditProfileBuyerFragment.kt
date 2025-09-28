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
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
            binding.ivProfile.setImageURI(it)
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
        viewModel.fetchUserData()
        observeViewModel()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnChangePhoto.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
        binding.btnSave.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val address = binding.etAddress.text.toString().trim()
            viewModel.updateUser(requireContext(), name, phone, address, selectedImageUri)
        }
    }

    private fun observeViewModel() {
        viewModel.userData.observe(viewLifecycleOwner) { data ->
            binding.etName.setText(data["name"] as? String)
            binding.etPhone.setText(data["phone"] as? String)
            binding.etAddress.setText(data["address"] as? String)

            val imageUrl = data["profileImageUrl"] as? String
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(binding.ivProfile)
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { status ->
            when {
                status.equals("Loading") -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                status.equals("Success") -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                status.startsWith("Error") -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}