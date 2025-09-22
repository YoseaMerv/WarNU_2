package com.imersa.warnu.ui.buyer.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentHomeBuyerBinding

class HomeBuyerFragment : Fragment() {

    private var _binding: FragmentHomeBuyerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HomeBuyerAdapter
    private lateinit var bannerAdapter: BannerAdapter

    private val viewModel: HomeBuyerViewModel by viewModels()

    private var currentPage = 0
    private val handler = Handler(Looper.getMainLooper())

    private val autoSlideRunnable = object : Runnable {
        override fun run() {
            val nextItem = binding.viewPagerBanner.currentItem + 1
            binding.viewPagerBanner.setCurrentItem(nextItem, true)
            handler.postDelayed(this, 3000)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBuyerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupBanner()
        observeViewModel()

        // Load data awal
        viewModel.loadProducts()

        // Search listener
        binding.etSearch.addTextChangedListener { text ->
            viewModel.searchProducts(text.toString())
        }
    }

    private fun setupRecyclerView() {
        adapter = HomeBuyerAdapter(
            onItemClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(
                    R.id.action_homeBuyerFragment_to_detailProductFragment,
                    bundle
                )
            },
            onAddToCartClick = { product ->
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@HomeBuyerAdapter

                val cartRef = FirebaseFirestore.getInstance()
                    .collection("carts")
                    .document(userId)
                    .collection("items")
                    .document(product.id!!)

                FirebaseFirestore.getInstance().runTransaction { transaction ->
                    val snapshot = transaction.get(cartRef)
                    if (snapshot.exists()) {
                        val currentQty = snapshot.getLong("quantity") ?: 0
                        transaction.update(cartRef, "quantity", currentQty + 1)
                    } else {
                        val cartItem = hashMapOf(
                            "productId" to product.id,
                            "name" to product.name,
                            "price" to product.price,
                            "imageUrl" to product.imageUrl,
                            "quantity" to 1,
                            "sellerId" to product.sellerId
                        )
                        transaction.set(cartRef, cartItem)
                    }
                }.addOnSuccessListener {
                    Toast.makeText(requireContext(), "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal menambahkan item", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.rvProdukBuyer.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProdukBuyer.adapter = adapter
    }

    private fun setupBanner() {
        val banners = listOf(
            R.drawable.banner1,
            R.drawable.banner2,
            R.drawable.banner3
        )

        bannerAdapter = BannerAdapter(banners)
        binding.viewPagerBanner.adapter = bannerAdapter
        binding.viewPagerBanner.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        // Mulai dari tengah supaya bisa swipe kiri/kanan tanpa ujung
        val startPosition = Int.MAX_VALUE / 2
        binding.viewPagerBanner.setCurrentItem(startPosition, false)

        // Auto-slide jalan tiap 3 detik
        handler.postDelayed(autoSlideRunnable, 3000)
    }

    private fun observeViewModel() {
        viewModel.filteredProducts.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.emptyState.observe(viewLifecycleOwner) {
            binding.tvEmptyState.visibility = if (it) View.VISIBLE else View.GONE
            binding.rvProdukBuyer.visibility = if (it) View.GONE else View.VISIBLE
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoSlideRunnable)
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(autoSlideRunnable)
        handler.postDelayed(autoSlideRunnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(autoSlideRunnable)
    }
}
