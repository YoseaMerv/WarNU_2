package com.imersa.warnu.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentHomeBuyerBinding

class HomeBuyerFragment : Fragment() {

    private var _binding: FragmentHomeBuyerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HomeBuyerAdapter
    private val viewModel: HomeBuyerViewModel by viewModels()

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
                viewModel.addToCart(product,
                    onSuccess = {
                        Toast.makeText(requireContext(), "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                    },
                    onError = {
                        Toast.makeText(requireContext(), "Gagal menambahkan item", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
        binding.rvProdukBuyer.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProdukBuyer.adapter = adapter
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
