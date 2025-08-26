package com.imersa.warnu.ui.seller.profile

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.imersa.warnu.databinding.FragmentProfileSellerBinding

class ProfileSellerFragment : Fragment() {

    private var _binding: FragmentProfileSellerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileSellerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadSellerProfile()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.sellerName.observe(viewLifecycleOwner) { name ->
            binding.tvNamaValue.text = name
        }

        viewModel.storeName.observe(viewLifecycleOwner) { store ->
            binding.tvShopName.text = store
        }

        viewModel.phone.observe(viewLifecycleOwner) { phone ->
            binding.tvTeleponValue.text = phone
        }

        viewModel.address.observe(viewLifecycleOwner) { address ->
            binding.tvAlamatValue.text = address
        }

        viewModel.sellerEmail.observe(viewLifecycleOwner) { email ->
            binding.tvEmailValue.text = email
        }

        viewModel.photoUrl.observe(viewLifecycleOwner) { url ->
            if (!url.isNullOrEmpty()) {
                Glide.with(this)
                    .load(url)
                    .centerCrop()
                    .into(binding.ivFotoProfil)
            } else {
                binding.ivFotoProfil.setImageResource(R.color.darker_gray) // fallback
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