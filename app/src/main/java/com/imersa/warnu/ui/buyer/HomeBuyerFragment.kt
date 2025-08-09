package com.imersa.warnu.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imersa.warnu.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeBuyerFragment : Fragment() {

    private lateinit var rvProdukBuyer: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var bannerImage: ImageView

    private val viewModel: HomeBuyerViewModel by viewModels()
    private lateinit var adapterProduk: ProdukBuyerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_buyer, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Init Views
        rvProdukBuyer = view.findViewById(R.id.rvProdukBuyer)
        etSearch = view.findViewById(R.id.etSearch)
        bannerImage = view.findViewById(R.id.bannerImage)

        // Setup RecyclerView
        adapterProduk = ProdukBuyerAdapter { produk ->
            val bundle = Bundle().apply {
                putString("productId", produk.id ?: "")
                putString("name", produk.name)
                putString("price", produk.price?.toString() ?: "0")
                putString("description", produk.description)
                putInt("stock", produk.stock ?: 0)
                putString("imageUrl", produk.imageUrl)
                putString("category", produk.category)
            }
            findNavController().navigate(R.id.detailProductFragment, bundle)
        }

        rvProdukBuyer.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProdukBuyer.adapter = adapterProduk

        // Observasi data dari ViewModel
        viewModel.produkList.observe(viewLifecycleOwner) { listProduk ->
            adapterProduk.submitList(listProduk)
        }

        // Search action
        etSearch.addTextChangedListener { editable ->
            val keyword = editable.toString()
            viewModel.searchProduk(keyword)
        }


        // Banner click (optional)
        bannerImage.setOnClickListener {
            Toast.makeText(requireContext(), "Banner diklik", Toast.LENGTH_SHORT).show()
        }

        // Load produk awal
        viewModel.loadProduk()
    }
}
