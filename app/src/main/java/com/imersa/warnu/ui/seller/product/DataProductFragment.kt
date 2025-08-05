package com.imersa.warnu.ui.seller.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentProductDataBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DataProductFragment : Fragment() {

    private var _binding: FragmentProductDataBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SellerProductViewModel by viewModels()

    @Inject lateinit var auth: FirebaseAuth

    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeProducts()

        val sellerId = auth.currentUser?.uid ?: return
        viewModel.fetchProducts(sellerId)
    }

    private fun setupRecyclerView()  {
        adapter = ProductAdapter(productList) { product ->
            val bundle = Bundle().apply {
                putString("productId", product.id)
                putString("name", product.name)
                putString("price", product.price.toString())
                putString("description", product.description)
                putInt("stock", product.stock)
                putString("imageUrl", product.imageUrl)
            }
            // Arahkan ke DetailProductFragment
            parentFragmentManager.setFragmentResult("productDetail", bundle)
            val navController = requireActivity().findNavController(R.id.fragment_container_seller)
            navController.navigate(R.id.detailProductFragment, bundle)
        }

        binding.rvProductSeller.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProductSeller.adapter = adapter
    }

    private fun observeProducts() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            productList.clear()
            productList.addAll(products)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
