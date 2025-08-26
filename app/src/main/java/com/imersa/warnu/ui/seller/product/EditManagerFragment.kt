package com.imersa.warnu.ui.seller.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.imersa.warnu.R
import com.imersa.warnu.data.model.Product
import com.imersa.warnu.databinding.FragmentEditManagerBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditManageFragment : Fragment() {

    private var _binding: FragmentEditManagerBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var auth: FirebaseAuth

    private lateinit var adapter: ProductSellerAdapter
    private val productList = mutableListOf<Product>()

    private val viewModel: EditManageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        val sellerId = auth.currentUser?.uid
        if (sellerId != null) {
            viewModel.fetchProducts(sellerId)
        } else {
            Toast.makeText(requireContext(), "User belum login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductSellerAdapter(
            products = productList,
            onItemClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id ?: "")
                }
                findNavController().navigate(R.id.detailProductFragment, bundle)
            },
            onEditClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id ?: "")
                }
                findNavController().navigate(R.id.editProductFragment, bundle)
            },
            onDeleteClick = onDeleteClick@{ product ->
                val productId = product.id ?: ""
                if (productId.isBlank()) {
                    Toast.makeText(requireContext(), "Produk tidak valid", Toast.LENGTH_SHORT).show()
                    return@onDeleteClick
                }
                viewModel.deleteProduct(productId) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Produk dihapus", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Gagal menghapus produk", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            layoutResId = R.layout.item_edit_product
        )

        binding.ProductSeller.layoutManager = GridLayoutManager(requireContext(), 1)
        binding.ProductSeller.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productList.clear()
            productList.addAll(products)
            adapter.notifyDataSetChanged()
        }
        viewModel.errorMessage.observe(viewLifecycleOwner) { msg ->
            msg?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
