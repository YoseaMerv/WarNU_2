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


@AndroidEntryPoint
class ManageProductFragment : Fragment() {

    private var _binding: FragmentProductManageBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ProductAdapter
    private val productList = mutableListOf<Product>()

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductManageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ProductAdapter(productList)
        binding.rvProductSeller.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvProductSeller.adapter = adapter

        loadProducts()
    }

    private fun loadProducts() {
        val sellerId = auth.currentUser?.uid ?: return
        firestore.collection("products")
            .whereEqualTo("sellerId", sellerId)
            .get()
            .addOnSuccessListener { snapshot ->
                productList.clear()
                for (doc in snapshot.documents) {
                    val product = doc.toObject(Product::class.java)
                    product?.let { productList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
