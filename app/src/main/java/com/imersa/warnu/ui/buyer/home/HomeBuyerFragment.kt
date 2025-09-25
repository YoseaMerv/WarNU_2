package com.imersa.warnu.ui.buyer.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentHomeBuyerBinding
import com.imersa.warnu.ui.buyer.product.BannerAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.abs

@AndroidEntryPoint
class HomeBuyerFragment : Fragment() {

    private var _binding: FragmentHomeBuyerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeBuyerViewModel by viewModels()
    private lateinit var productAdapter: HomeBuyerAdapter

    private val sliderHandler = Handler(Looper.getMainLooper())
    private lateinit var sliderRunnable: Runnable

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
        setupSearchView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        productAdapter = HomeBuyerAdapter(
            onItemClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                }
                findNavController().navigate(R.id.nav_product_detail, bundle)
            }
        )
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = productAdapter
        }
    }

    private fun setupBanner() {
        val bannerImages = listOf(R.drawable.banner1, R.drawable.banner2, R.drawable.banner3)
        val bannerAdapter = BannerAdapter(bannerImages)
        binding.vpBanner.adapter = bannerAdapter

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(MarginPageTransformer(40))
        compositePageTransformer.addTransformer { page, position ->
            val r = 1 - abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        binding.vpBanner.setPageTransformer(compositePageTransformer)

        sliderRunnable = Runnable {
            val currentItem = binding.vpBanner.currentItem
            val nextItem = if (currentItem == bannerAdapter.itemCount - 1) 0 else currentItem + 1
            binding.vpBanner.setCurrentItem(nextItem, true)
        }
    }

    private fun startAutoSlider(count: Int) {
        if (count > 1) { // Hanya mulai jika ada lebih dari 1 banner
            sliderRunnable.let {
                sliderHandler.postDelayed(it, 3000)
            }

            binding.vpBanner.registerOnPageChangeCallback(object :
                androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    sliderHandler.removeCallbacks(sliderRunnable)
                    sliderHandler.postDelayed(sliderRunnable, 3000)
                }
            })
        }
    }

    private fun stopAutoSlider() {
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    // Fungsi search yang diperbaiki
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Tidak perlu aksi khusus karena pencarian sudah real-time
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Panggil fungsi search di ViewModel setiap kali teks berubah
                viewModel.searchProducts(newText.orEmpty())
                return true
            }
        })
    }


    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            if (products.isEmpty()) {
                binding.tvNoProducts.visibility = View.VISIBLE
                binding.rvProducts.visibility = View.GONE
            } else {
                binding.tvNoProducts.visibility = View.GONE
                binding.rvProducts.visibility = View.VISIBLE
                productAdapter.submitList(products)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        stopAutoSlider()
    }

    override fun onResume() {
        super.onResume()
        (binding.vpBanner.adapter?.itemCount)?.let { startAutoSlider(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoSlider()
        _binding = null
    }
}