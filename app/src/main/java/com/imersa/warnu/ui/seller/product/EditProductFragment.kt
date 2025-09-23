package com.imersa.warnu.ui.seller.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentEditProductBinding

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditProductViewModel by viewModels()

    private var imageUri: Uri? = null
    private var productId: String? = null
    private var currentImageUrl: String? = null


    private val pickImageLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.data
                binding.ivKategoriPreview.visibility = View.VISIBLE
                binding.ivKategoriPreview.setImageURI(imageUri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)

        productId = arguments?.getString("productId")
        currentImageUrl = arguments?.getString("imageUrl")

        if (productId == null) {
            Toast.makeText(requireContext(), "ID Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return binding.root
        }

        setupUI()
        observeViewModel()

        viewModel.loadProduct(productId!!)

        return binding.root
    }

    private fun setupUI() {
        val kategoriList = listOf("Fashion", "Electronics", "Home", "Toys")
        val adapterKategori =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, kategoriList)
        binding.actvKategori.setAdapter(adapterKategori)

        loadImage(currentImageUrl)

        binding.btnPilihGambarBaru.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        binding.btnSimpanProduk.setOnClickListener { simpanPerubahan() }
    }

    private fun observeViewModel() {
        viewModel.productData.observe(viewLifecycleOwner) { product ->
            product?.let {
                binding.etNamaProduk.setText(it.name)
                binding.etDeskripsi.setText(it.description)
                binding.etHarga.setText(it.price.toString())
                binding.etStok.setText(it.stock.toString())
                binding.actvKategori.setText(it.category, false)
                currentImageUrl = it.imageUrl
                loadImage(currentImageUrl)
            }
        }

        viewModel.uploadStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess { imageUrl ->
                productId?.let { pid -> updateProductInFirestore(pid, imageUrl) }
            }
            result.onFailure {
                Toast.makeText(
                    requireContext(), "Gagal upload gambar: ${it.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }

        viewModel.updateStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Produk berhasil diupdate", Toast.LENGTH_SHORT)
                    .show()
                parentFragmentManager.popBackStack()
            }
            result.onFailure {
                Toast.makeText(
                    requireContext(), "Gagal update produk: ${it.message}", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun simpanPerubahan() {
        val nama = binding.etNamaProduk.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        val harga = binding.etHarga.text.toString().trim().toDoubleOrNull()
        val stok = binding.etStok.text.toString().trim().toLongOrNull()
        val kategori = binding.actvKategori.text.toString().trim()

        if (nama.isEmpty() || deskripsi.isEmpty() || harga == null || stok == null || kategori.isEmpty()) {
            Toast.makeText(requireContext(), "Semua field harus diisi!", Toast.LENGTH_SHORT).show()
            return
        }

        val sellerUid = FirebaseAuth.getInstance().currentUser?.uid
        if (sellerUid == null) {
            Toast.makeText(requireContext(), "User tidak terautentikasi", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            // Kirim UID seller ke ViewModel
            viewModel.uploadImageAndDeleteOld(imageUri!!, currentImageUrl, sellerUid)
        } else {
            updateProductInFirestore(productId!!, currentImageUrl ?: "")
        }
    }


    private fun updateProductInFirestore(productId: String, imageUrl: String) {
        val nama = binding.etNamaProduk.text.toString().trim()
        val deskripsi = binding.etDeskripsi.text.toString().trim()
        val harga = binding.etHarga.text.toString().trim().toDouble()
        val stok = binding.etStok.text.toString().trim().toLong()
        val kategori = binding.actvKategori.text.toString().trim()

        viewModel.updateProduct(productId, nama, deskripsi, harga, stok, kategori, imageUrl)
    }

    private fun loadImage(url: String?) {
        if (!url.isNullOrEmpty()) {
            binding.ivKategoriPreview.visibility = View.VISIBLE
            Glide.with(requireContext()).load(url).placeholder(R.drawable.placeholder_image)
                .into(binding.ivKategoriPreview)
        } else {
            binding.ivKategoriPreview.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
