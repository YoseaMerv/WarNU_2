package com.imersa.warnu.ui.buyer.cart

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.imersa.warnu.data.model.CartItem
import com.imersa.warnu.databinding.FragmentCartBinding
import com.imersa.warnu.ui.checkout.CheckoutActivity
import java.text.NumberFormat
import java.util.Locale

class CartFragment : Fragment() {

    private val cartViewModel: CartViewModel by viewModels()

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        observeCartItems()

        setupCheckoutButton()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(onUpdateQuantity = { cartItem, newQuantity ->
            cartViewModel.updateCartItemQuantity(cartItem.productId ?: "", newQuantity)
        }, onRemoveItem = { cartItem ->
            cartViewModel.removeCartItem(cartItem.productId ?: "")
        })
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun observeCartItems() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            binding.emptyCartLayout.isVisible = cartItems.isNullOrEmpty()
            binding.rvCart.isVisible = !cartItems.isNullOrEmpty()
            binding.checkoutLayout.isVisible = !cartItems.isNullOrEmpty()

            if (!cartItems.isNullOrEmpty()) {
                cartAdapter.submitList(cartItems)
                updateTotalPrice(cartItems)
            }
        }
    }

    private fun updateTotalPrice(cartItems: List<CartItem>) {
        val totalPrice = cartItems.sumOf { (it.price ?: 0.0) * it.quantity }
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        binding.tvTotalPrice.text = numberFormat.format(totalPrice)
    }

    private fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            val cartItems = cartViewModel.cartItems.value

            if (cartItems.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Keranjang Anda kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalAmount = cartItems.sumOf { (it.price ?: 0.0) * it.quantity }

            val intent = Intent(requireContext(), CheckoutActivity::class.java).apply {
                putExtra("TOTAL_AMOUNT", totalAmount)
            }
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}