package com.imersa.warnu.ui.buyer.cart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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

    // Gunakan viewModels delegate untuk mendapatkan instance ViewModel
    private val cartViewModel: CartViewModel by viewModels()

    // Gunakan nullable backing property untuk view binding agar aman dari memory leak
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private val checkoutLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Kode ini akan berjalan saat CheckoutActivity ditutup
        if (result.resultCode == Activity.RESULT_OK) {
            // Pembayaran selesai (berhasil, gagal, atau dibatalkan)
            // ViewModel akan otomatis memperbarui UI karena kita menggunakan addSnapshotListener
            // Kita tidak perlu melakukan apa-apa di sini, cukup biarkan UI refresh secara otomatis
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup RecyclerView dan Adapter
        setupRecyclerView()

        // Amati perubahan pada data keranjang dari ViewModel
        observeCartItems()

        // Setup listener untuk tombol checkout
        setupCheckoutButton()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            // Callback untuk memperbarui kuantitas item
            onUpdateQuantity = { cartItem, newQuantity ->
                cartViewModel.updateCartItemQuantity(cartItem.productId ?: "", newQuantity)
            },
            // Callback untuk menghapus item
            onRemoveItem = { cartItem ->
                cartViewModel.removeCartItem(cartItem.productId ?: "")
            }
        )
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun observeCartItems() {
        cartViewModel.cartItems.observe(viewLifecycleOwner) { cartItems ->
            // Tampilkan atau sembunyikan pesan "keranjang kosong"
            binding.emptyCartLayout.isVisible = cartItems.isNullOrEmpty()
            binding.rvCart.isVisible = !cartItems.isNullOrEmpty()
            binding.checkoutLayout.isVisible = !cartItems.isNullOrEmpty()

            if (!cartItems.isNullOrEmpty()) {
                // Kirim data ke adapter
                cartAdapter.submitList(cartItems)
                // Hitung dan tampilkan total harga
                updateTotalPrice(cartItems)
            }
        }
    }

    private fun updateTotalPrice(cartItems: List<CartItem>) {
        // Hitung total harga dari semua item di keranjang
        val totalPrice = cartItems.sumOf { (it.price ?: 0.0) * it.quantity }

        // Format harga ke dalam format Rupiah (Rp)
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        binding.tvTotalPrice.text = numberFormat.format(totalPrice)
    }

    private fun setupCheckoutButton() {
        binding.btnCheckout.setOnClickListener {
            val cartItems = cartViewModel.cartItems.value

            // Validasi jika keranjang kosong
            if (cartItems.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Keranjang Anda kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hitung total harga untuk dikirim ke activity selanjutnya
            val totalAmount = cartItems.sumOf { (it.price ?: 0.0) * it.quantity }

            // Buat intent untuk memulai CheckoutActivity
            val intent = Intent(requireContext(), CheckoutActivity::class.java).apply {
                putExtra("TOTAL_AMOUNT", totalAmount)
            }
            startActivity(intent)
        }
    }

    // Wajib untuk membersihkan referensi binding saat view dihancurkan
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}