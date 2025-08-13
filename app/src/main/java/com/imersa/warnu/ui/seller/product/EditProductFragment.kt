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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)

        // Ambil data dari arguments
        arguments?.let {
            productId = it.getString("productId")
            binding.etNamaProduk.setText(it.getString("name"))
            binding.etDeskripsi.setText(it.getString("description"))
            binding.etHarga.setText(it.getDouble("price").toString())
            binding.etStok.setText(it.getLong("stock").toString())
            binding.actvKategori.setText(it.getString("category"))
            currentImageUrl = it.getString("imageUrl")

            if (!currentImageUrl.isNullOrEmpty()) {
                binding.ivKategoriPreview.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(currentImageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.ivKategoriPreview)
            }
        }

        // Setup kategori dropdown
        val kategoriList = listOf("Fashion", "Electronics", "Home", "Toys")
        val adapterKategori = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, kategoriList)
        binding.actvKategori.setAdapter(adapterKategori)

        // Pilih gambar baru
        binding.btnPilihGambarBaru.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        // Simpan produk
        binding.btnSimpanProduk.setOnClickListener {
            simpanPerubahan()
        }

        observeViewModel()

        return binding.root
    }



    private fun observeViewModel() {

        productId = arguments?.getString("productId")
        if (productId != null) {
            viewModel.loadProduct(productId!!)
        }


        viewModel.uploadStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess { imageUrl ->
                productId?.let { pid ->
                    updateProductInFirestore(pid, imageUrl)
                }
            }
            result.onFailure {
                Toast.makeText(requireContext(), "Gagal upload gambar: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.productData.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                binding.etNamaProduk.setText(product.name)
                binding.etDeskripsi.setText(product.description)
                binding.etHarga.setText(product.price.toString())
                binding.etStok.setText(product.stock.toString())
                binding.actvKategori.setText(product.category)
                currentImageUrl = product.imageUrl
                if (!currentImageUrl.isNullOrEmpty()) {
                    binding.ivKategoriPreview.visibility = View.VISIBLE
                    Glide.with(requireContext())
                        .load(currentImageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .into(binding.ivKategoriPreview)
                }
            }
        }


        viewModel.updateStatus.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                Toast.makeText(requireContext(), "Produk berhasil diupdate", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            result.onFailure {
                Toast.makeText(requireContext(), "Gagal update produk: ${it.message}", Toast.LENGTH_SHORT).show()
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

        if (imageUri != null) {
            viewModel.uploadImage(imageUri!!)
        } else {
            // Gunakan currentImageUrl kalau gambar tidak diganti
            productId?.let { pid ->
                updateProductInFirestore(pid, currentImageUrl ?: "")
            }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
