package com.imersa.warnu.ui.seller.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.imersa.warnu.databinding.FragmentAddProductBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddProductViewModel by viewModels()

    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Pilih gambar
        binding.btnPilihGambar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }

        // Simpan produk
        binding.btnSimpanProduk.setOnClickListener {
            val name = binding.etNamaProduk.text.toString()
            val price = binding.etHarga.text.toString()
            val description = binding.etDeskripsi.text.toString()
            val stock = binding.etStok.text.toString()
            val category = binding.actvKategori.text.toString()

            viewModel.addProduct(
                name,
                price,
                description,
                stock,
                category,
                selectedImageUri
            )
        }

        // Observasi state
        lifecycleScope.launchWhenStarted {
            viewModel.state.collectLatest { state ->
                when (state) {
                    is AddProductState.Idle -> Unit
                    is AddProductState.Loading -> {
                        Toast.makeText(requireContext(), "Mengunggah...", Toast.LENGTH_SHORT).show()
                    }
                    is AddProductState.Success -> {
                        Toast.makeText(requireContext(), "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                        viewModel.resetState()
                        resetForm()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            if (selectedImageUri != null) {
                binding.ivKategoriPreview.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(selectedImageUri)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.ivKategoriPreview)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 100
    }
}
