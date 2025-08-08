package com.imersa.warnu.ui.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
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
            Toast.makeText(requireContext(), "Klik: ${produk.nama}", Toast.LENGTH_SHORT).show()
        }
        rvProdukBuyer.layoutManager = GridLayoutManager(requireContext(), 2)
        rvProdukBuyer.adapter = adapterProduk

        // Observasi data dari ViewModel
        viewModel.produkList.observe(viewLifecycleOwner) { listProduk ->
            adapterProduk.submitList(listProduk)
        }

        // Search action
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                val keyword = etSearch.text.toString()
                viewModel.searchProduk(keyword)
                true
            } else {
                false
            }
        }

        // Banner click (optional)
        bannerImage.setOnClickListener {
            Toast.makeText(requireContext(), "Banner diklik", Toast.LENGTH_SHORT).show()
        }

        // Load produk awal
        viewModel.loadProduk()
    }
}
