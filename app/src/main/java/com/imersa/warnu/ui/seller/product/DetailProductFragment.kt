package com.imersa.warnu.ui.seller.product

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentDetailProductBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.text.NumberFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@AndroidEntryPoint
class DetailProductFragment : Fragment() {

    private var _binding: FragmentDetailProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailProductViewModel by viewModels()
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val productId = arguments?.getString("productId")
        if (productId == null) {
            Toast.makeText(requireContext(), "Product ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        // Ambil role dari Firestore
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role")
                        if (role == "buyer") {
                            binding.fabAddToCart.visibility = View.VISIBLE
                        } else {
                            binding.fabAddToCart.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal ambil role user", Toast.LENGTH_SHORT).show()
                }
        }

        binding.fabAddToCart.setOnClickListener {
            val currentProduct = viewModel.product.value
            val userId = auth.currentUser?.uid

            if (currentProduct?.id != null && userId != null) {
                firestore.collection("carts")
                    .document(userId)
                    .collection("items")
                    .document(currentProduct.id)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            // ... (logika untuk update quantity tidak perlu diubah)
                            val currentQty = doc.getLong("quantity") ?: 0
                            firestore.collection("carts")
                                .document(userId)
                                .collection("items")
                                .document(currentProduct.id)
                                .update("quantity", currentQty + 1)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Qty ditambah", Toast.LENGTH_SHORT).show()
                                }
                            // ...
                        } else {
                            // --- PERUBAHAN DI SINI ---
                            // Belum ada → tambahkan baru
                            val cartItem = hashMapOf(
                                "productId" to currentProduct.id,
                                "name" to currentProduct.name,
                                "price" to currentProduct.price,
                                "quantity" to 1,
                                "sellerId" to currentProduct.sellerId, // 💡 TAMBAHKAN BARIS INI
                                "imageUrl" to currentProduct.imageUrl // Opsional: bagus untuk ditampilkan di cart
                            )
                            // --- AKHIR PERUBAHAN ---

                            firestore.collection("carts")
                                .document(userId)
                                .collection("items")
                                .document(currentProduct.id)
                                .set(cartItem)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Gagal tambah ke cart", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            } else {
                Toast.makeText(requireContext(), "Produk belum dimuat", Toast.LENGTH_SHORT).show()
            }
        }


        viewModel.product.observe(viewLifecycleOwner) { product ->
            val formattedPrice = NumberFormat.getNumberInstance(Locale("id", "ID")).format(product.price)

            binding.tvDetailNamaProduk.text = product.name
            binding.tvDetailHargaProduk.text = "Rp$formattedPrice"
            binding.tvDetailDeskripsi.text = product.description
            binding.chipKategori.text = product.category
            binding.chipStok.text = "Stok: ${product.stock}"

            Glide.with(this)
                .load(product.imageUrl)
                .placeholder(R.drawable.placeholder_image)
                .into(binding.ivDetailGambarProduk)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.loadingLayout.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.contentLayout.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.getProductById(productId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
