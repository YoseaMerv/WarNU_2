package com.imersa.warnu.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.databinding.FragmentHomeBuyerBinding

class HomeBuyerFragment : Fragment() {

    private var _binding: FragmentHomeBuyerBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: HomeBuyerAdapter
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

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
        loadProducts()
    }

    private fun setupRecyclerView() {
        adapter = HomeBuyerAdapter { product ->
            addToCart(product)
        }
        binding.rvProdukBuyer.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProdukBuyer.adapter = adapter
    }

    private fun loadProducts() {
        showLoading(true)
        firestore.collection("products")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    showLoading(false)
                    Toast.makeText(requireContext(), "Gagal memuat data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val productList = snapshot.toObjects(ProdukBuyer::class.java)
                    adapter.submitList(productList)
                    showEmptyState(false)
                } else {
                    adapter.submitList(emptyList())
                    showEmptyState(true)
                }
                showLoading(false)
            }
    }

    private fun addToCart(product: ProdukBuyer) {
        val userId = auth.currentUser?.uid ?: return
        val cartRef = firestore.collection("cart").document(userId).collection("items")

        val cartItem = CartItem(
            id = product.id ?: "",
            name = product.name ?: "",
            price = product.price ?: 0.0,
            imageUrl = product.imageUrl ?: "",
            quantity = 1
        )

        // Cek apakah produk sudah ada di keranjang
        cartRef.document(product.id ?: "").get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Update quantity
                    val currentQty = document.getLong("quantity")?.toInt() ?: 1
                    cartRef.document(product.id ?: "")
                        .update("quantity", currentQty + 1)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Jumlah diperbarui", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Tambah produk baru
                    cartRef.document(product.id ?: "")
                        .set(cartItem)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Ditambahkan ke keranjang", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvProdukBuyer.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
