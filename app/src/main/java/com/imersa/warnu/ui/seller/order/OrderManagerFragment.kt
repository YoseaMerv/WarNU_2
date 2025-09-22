package com.imersa.warnu.ui.seller.order

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.imersa.warnu.databinding.FragmentSellerOrdersBinding

class OrderManagerFragment : Fragment() {

    private var _binding: FragmentSellerOrdersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OrderSellerViewModel by viewModels()
    private lateinit var adapter: OrderSellerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔹 Pasang adapter kosong dulu
        adapter = OrderSellerAdapter(mutableListOf())
        binding.rvOrders.adapter = adapter

        // 🔹 Observe orders
        viewModel.orders.observe(viewLifecycleOwner) { orders ->
            if (orders.isNotEmpty()) {
                adapter.updateData(orders)
                binding.rvOrders.visibility = View.VISIBLE
                binding.emptyLayout.visibility = View.GONE
            } else {
                binding.rvOrders.visibility = View.GONE
                binding.emptyLayout.visibility = View.VISIBLE
            }
        }

        viewModel.fetchOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


