package com.imersa.warnu.ui.seller.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.databinding.FragmentProductManageBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ManageProductFragment : Fragment() {

    private var _binding: FragmentProductManageBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var productRepository: ProductRepository
    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(productList)
        binding.rvProductSeller.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProductSeller.adapter = adapter

        val sellerId = auth.currentUser?.uid ?: return

        productRepository.getProductsBySeller(sellerId).observe(viewLifecycleOwner) { products ->
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

