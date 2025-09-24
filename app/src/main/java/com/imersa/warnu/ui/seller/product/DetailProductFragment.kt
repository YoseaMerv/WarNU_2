package com.imersa.warnu.ui.seller.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Product
import com.imersa.warnu.databinding.FragmentDetailProductBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class DetailProductFragment : Fragment() {
    private var _binding: FragmentDetailProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailProductViewModel by viewModels()
    private var currentQuantity = 1
    private var currentProduct: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { activity?.onBackPressedDispatcher?.onBackPressed() }

        val productId = arguments?.getString("productId")
        if (productId == null) {
            Toast.makeText(context, "Product ID not found", Toast.LENGTH_SHORT).show()
            activity?.onBackPressedDispatcher?.onBackPressed()
            return
        }

        viewModel.loadProduct(productId)
        viewModel.loadUserRole() // Panggil fungsi untuk memuat role
        observeViewModel()
        setupClickListeners()
    }
    private fun observeViewModel() {
        viewModel.product.observe(viewLifecycleOwner) { product ->
            product?.let {
                currentProduct = it
                binding.collapsingToolbar.title = it.name
                binding.tvProductName.text = it.name
                binding.tvProductDescription.text = it.description
                val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                binding.tvProductPrice.text = formatter.format(it.price ?: 0.0)
                binding.tvProductStock.text = "Stock: ${it.stock ?: 0}"
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.ivProductImage)
            }
        }

        viewModel.addToCartStatus.observe(viewLifecycleOwner) { status ->
            status?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.onStatusMessageShown()
            }
        }
        viewModel.userRole.observe(viewLifecycleOwner) { role ->
            val isBuyer = role == "buyer"

            // Tampilkan/sembunyikan tombol berdasarkan role
            binding.fabAddToCart.isVisible = isBuyer
            binding.btnIncreaseQuantity.isVisible = isBuyer
            binding.btnDecreaseQuantity.isVisible = isBuyer
            binding.tvQuantity.isVisible = isBuyer
        }
    }

    private fun setupClickListeners() {
        binding.btnIncreaseQuantity.setOnClickListener {
            currentQuantity++
            binding.tvQuantity.text = currentQuantity.toString()
        }

        binding.btnDecreaseQuantity.setOnClickListener {
            if (currentQuantity > 1) {
                currentQuantity--
                binding.tvQuantity.text = currentQuantity.toString()
            }
        }

        binding.fabAddToCart.setOnClickListener {
            currentProduct?.let { product ->
                viewModel.addToCart(product, currentQuantity)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}