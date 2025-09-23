package com.imersa.warnu.ui.seller.product

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentAddProductBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddProductViewModel by viewModels()

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it

                binding.ivKategoriPreview.apply {
                    visibility = View.VISIBLE
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }

                Glide.with(requireContext()).load(it).placeholder(R.drawable.placeholder_image)
                    .fitCenter().into(binding.ivKategoriPreview)
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categories = listOf("Fashion", "Electronics", "Home", "Toys")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item_dropdown, categories)
        binding.actvKategori.setAdapter(adapter)

        binding.btnPilihGambar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSimpanProduk.setOnClickListener {
            val name = binding.etNamaProduk.text.toString().trim()
            val price = binding.etHarga.text.toString().trim()
            val description = binding.etDeskripsi.text.toString().trim()
            val stock = binding.etStok.text.toString().trim()
            val category = binding.actvKategori.text.toString().trim()

            viewModel.addProduct(
                name, price, description, stock, category, selectedImageUri
            )
        }
        @Suppress("DEPRECATION")
        lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is AddProductState.Idle -> Unit
                    is AddProductState.Loading -> {
                        Toast.makeText(requireContext(), "Mengunggah...", Toast.LENGTH_SHORT).show()
                    }

                    is AddProductState.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Produk berhasil ditambahkan",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetState()
                        resetForm()
                        findNavController().navigate(R.id.HomeSellerFragment)
                    }

                    is AddProductState.Error -> {
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                    }
                }
            }
        }
    }

    private fun resetForm() {
        with(binding) {
            etNamaProduk.text?.clear()
            etHarga.text?.clear()
            etDeskripsi.text?.clear()
            etStok.text?.clear()
            actvKategori.text?.clear()
            selectedImageUri = null
            ivKategoriPreview.setImageDrawable(null)
            ivKategoriPreview.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
