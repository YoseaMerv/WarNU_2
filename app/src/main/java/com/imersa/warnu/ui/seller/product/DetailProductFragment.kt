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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.appcompat.widget.Toolbar
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Product
import com.imersa.warnu.databinding.FragmentDetailProductBinding
import com.imersa.warnu.ui.buyer.main.MainBuyerActivity
import com.imersa.warnu.ui.seller.main.MainSellerActivity
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
    private var defaultTitle: CharSequence? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appCompatActivity = requireActivity() as AppCompatActivity
        val actionBar = appCompatActivity.supportActionBar

        // Simpan title lama dan hide ActionBar Activity
        defaultTitle = actionBar?.title
        actionBar?.hide()

        // Pasang toolbar fragment
        appCompatActivity.setSupportActionBar(binding.toolbarDetail)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.toolbarDetail.setNavigationOnClickListener {
            activity?.onBackPressedDispatcher?.onBackPressed()
        }

        val productId = arguments?.getString("productId")
        if (productId == null) {
            Toast.makeText(context, "Product ID not found", Toast.LENGTH_SHORT).show()
            activity?.onBackPressedDispatcher?.onBackPressed()
            return
        }

        viewModel.loadProduct(productId)
        viewModel.loadUserRole()
        observeViewModel()
        setupClickListeners()
    }


    private fun observeViewModel() {
        viewModel.product.observe(viewLifecycleOwner) { product ->
            product?.let {
                currentProduct = it
                binding.tvProductName.text = it.name
                binding.tvProductDescription.text = it.description
                val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID"))
                binding.tvProductPrice.text = formatter.format(it.price ?: 0.0)
                binding.tvProductStock.text = "Stock: ${it.stock ?: 0}"
                Glide.with(this)
                    .load(it.imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.ivProductImage)
                binding.collapsingToolbar.title = it.name
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
            binding.fabAddToCart.isVisible = isBuyer
            binding.btnIncreaseQuantity.isVisible = isBuyer
            binding.btnDecreaseQuantity.isVisible = isBuyer
            binding.tvQuantity.isVisible = isBuyer
        }
    }

    private fun setupClickListeners() {
        binding.btnIncreaseQuantity.setOnClickListener {
            // Cek apakah stok produk ada dan apakah kuantitas saat ini kurang dari stok
            currentProduct?.stock?.let { stock ->
                if (currentQuantity < stock) {
                    currentQuantity++
                    binding.tvQuantity.text = currentQuantity.toString()
                } else {
                    // Tampilkan pesan jika stok sudah maksimum
                    Toast.makeText(context, "You have reached the maximum stock", Toast.LENGTH_SHORT).show()
                }
            }
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

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar?.title = defaultTitle
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val appCompatActivity = requireActivity() as AppCompatActivity

        when (viewModel.userRole.value) {
            "seller" -> {
                val mainToolbar = appCompatActivity.findViewById<Toolbar>(R.id.seller_toolbar)
                mainToolbar?.let {
                    appCompatActivity.setSupportActionBar(it)
                    appCompatActivity.supportActionBar?.show()
                }

                val navHostFragment = appCompatActivity.supportFragmentManager
                    .findFragmentById(R.id.fragment_container_seller) as? NavHostFragment

                navHostFragment?.let { navController ->
                    appCompatActivity.setupActionBarWithNavController(
                        navController.navController,
                        (requireActivity() as MainSellerActivity).appBarConfiguration
                    )
                }
            }

            "buyer" -> {
                val mainToolbar = appCompatActivity.findViewById<Toolbar>(R.id.buyer_toolbar)
                mainToolbar?.let {
                    appCompatActivity.setSupportActionBar(it)
                    appCompatActivity.supportActionBar?.show()
                }

                val navHostFragment = appCompatActivity.supportFragmentManager
                    .findFragmentById(R.id.fragment_container_buyer) as? NavHostFragment

                navHostFragment?.let { navController ->
                    appCompatActivity.setupActionBarWithNavController(
                        navController.navController,
                        (requireActivity() as MainBuyerActivity).appBarConfiguration
                    )
                }
            }
        }

        _binding = null
    }

}
