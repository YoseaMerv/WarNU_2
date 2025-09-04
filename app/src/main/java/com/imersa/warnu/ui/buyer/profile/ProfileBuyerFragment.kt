package com.imersa.warnu.ui.buyer.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentProfileBuyerBinding

class ProfileBuyerFragment : Fragment() {

    private var _binding: FragmentProfileBuyerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileBuyerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBuyerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadBuyerProfile()
        setupObservers()

        // Tambahkan listener untuk tombol edit
        binding.btnEditProfil.setOnClickListener {
            findNavController().navigate(R.id.action_profileBuyerFragment_to_editProfileBuyerFragment)
        }
    }

    private fun setupObservers() {
        viewModel.buyerName.observe(viewLifecycleOwner) { name ->
            binding.tvNamaValue.text = name
        }

        viewModel.phone.observe(viewLifecycleOwner) { phone ->
            binding.tvTeleponValue.text = phone
        }

        viewModel.address.observe(viewLifecycleOwner) { address ->
            binding.tvAlamatValue.text = address
        }

        viewModel.buyerEmail.observe(viewLifecycleOwner) { email ->
            binding.tvEmailValue.text = email
        }

        viewModel.photoUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this)
                    .load(url)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image) // fallback
                    .into(binding.ivFotoProfil)
            } else {
                binding.ivFotoProfil.setImageResource(com.google.android.material.R.drawable.ic_m3_chip_close) // fallback
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.isVisible = loading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}