package com.imersa.warnu.ui.buyer.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.imersa.warnu.databinding.FragmentOrderHistoryBinding // Buat layout ini

class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderHistoryViewModel by viewModels()
    private lateinit var orderAdapter: OrderHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderHistoryAdapter()
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = orderAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.isVisible = it
        }

        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            binding.emptyLayout.isVisible = orders.isEmpty() && !viewModel.isLoading.value!!
            orderAdapter.submitList(orders)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}