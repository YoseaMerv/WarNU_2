package com.imersa.warnu.ui.seller.product

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

@AndroidEntryPoint
class DetailProductFragment : Fragment() {

    private var _binding: FragmentDetailProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DetailProductViewModel by viewModels()

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
            if (isLoading) {
                binding.loadingLayout.visibility = View.VISIBLE
                binding.contentLayout.visibility = View.GONE
            } else {
                binding.loadingLayout.visibility = View.GONE
                binding.contentLayout.visibility = View.VISIBLE
            }
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

