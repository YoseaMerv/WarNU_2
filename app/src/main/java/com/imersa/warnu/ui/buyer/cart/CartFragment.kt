package com.imersa.warnu.ui.buyer.cart

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.imersa.warnu.data.model.CartItem
import com.imersa.warnu.databinding.FragmentCartBinding
import com.imersa.warnu.ui.checkout.CheckoutActivity
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale
import java.text.DecimalFormat

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()

        binding.btnCheckout.setOnClickListener {
            val cartItems = cartAdapter.currentList
            if (cartItems.isNotEmpty()) {
                val intent = Intent(requireContext(), CheckoutActivity::class.java)
                intent.putExtra("CART_ITEMS", Gson().toJson(cartItems))
                startActivity(intent)
            }
        }
    }


    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onIncrease = { cartItem ->
                viewModel.increaseCartItemQuantity(cartItem)
            },
            onDecrease = { cartItem ->
                if (cartItem.quantity > 1) {
                    cartItem.productId?.let {
                        viewModel.updateCartItemQuantity(it, cartItem.quantity - 1)
                    }
                } else {
                    showRemoveConfirmationDialog(cartItem)
                }
            },
            onRemove = { cartItem ->
                showRemoveConfirmationDialog(cartItem)
            }
        )
        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = cartAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            val isEmpty = items.isNullOrEmpty()
            // Menggunakan ID yang sekarang sudah benar
            binding.layoutEmptyCart.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvCartItems.visibility = if (isEmpty) View.GONE else View.VISIBLE
            binding.layoutCheckout.visibility = if (isEmpty) View.GONE else View.VISIBLE

            if (!isEmpty) {
                cartAdapter.submitList(items)
                updateTotalPrice(items)
            }
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                viewModel.onToastMessageShown()
            }
        }
    }

    private fun showRemoveConfirmationDialog(cartItem: CartItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Remove Item")
            .setMessage("Are you sure you want to remove '${cartItem.name}' from your cart?")
            .setPositiveButton("Remove") { dialog, _ ->
                cartItem.productId?.let {
                    viewModel.removeCartItem(it)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateTotalPrice(items: List<CartItem>) {
        val totalPrice = items.sumOf { (it.price ?: 0.0) * it.quantity }

        val formatter = NumberFormat.getCurrencyInstance(Locale("in", "ID")) as DecimalFormat
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0

        binding.tvTotalPrice.text = formatter.format(totalPrice)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}