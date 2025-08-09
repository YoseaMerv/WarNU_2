package com.imersa.warnu.ui.seller.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentHomeSellerBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeSellerFragment : Fragment() {

    private var _binding: FragmentHomeSellerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeSellerViewModel by viewModels()

    @Inject
    lateinit var auth: FirebaseAuth

    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()

        val sellerId = auth.currentUser?.uid ?: return
        viewModel.fetchProducts(sellerId)
    }

    private fun setupRecyclerView() {
        adapter = ProductAdapter(
            products = productList,
            onItemClick = { product ->
                val bundle = Bundle().apply {
                    putString("productId", product.id ?: "")
                    putString("name", product.name)
                    putString("price", product.price?.toString() ?: "0")
                    putString("description", product.description)
                    putInt("stock", product.stock ?: 0)
                    putString("imageUrl", product.imageUrl)
                    putString("category", product.category)
                }
                parentFragmentManager.setFragmentResult("productDetail", bundle)
                val navController = requireActivity().findNavController(R.id.fragment_container_seller)
                navController.navigate(R.id.detailProductFragment, bundle)
            },
            layoutResId = R.layout.item_product_dashboard
        )
        binding.rvProductSeller.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProductSeller.adapter = adapter
    }


    private fun observeViewModel() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productList.clear()
            productList.addAll(products)
            adapter.notifyDataSetChanged()

            binding.tvEmpty.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
            binding.rvProductSeller.visibility = if (products.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
