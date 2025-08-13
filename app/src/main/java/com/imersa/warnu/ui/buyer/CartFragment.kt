package com.imersa.warnu.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.imersa.warnu.databinding.FragmentCartBinding

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)

        setupRecyclerView()
        observeCartData()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(emptyList()) { cartItem ->
            cartViewModel.removeFromCart(cartItem)
        }
        binding.rvCart.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCart.adapter = adapter
    }

    private fun observeCartData() {
        cartViewModel.cartList.observe(viewLifecycleOwner) { cartItems ->
            adapter.updateCartList(cartItems)

            binding.tvEmptyCart.visibility = if (cartItems.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
