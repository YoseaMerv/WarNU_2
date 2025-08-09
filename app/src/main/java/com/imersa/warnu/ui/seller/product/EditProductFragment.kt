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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentEditProductBinding

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private var productId: String? = null
    private var currentImageUrl: String? = null

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Ambil data dari arguments (dikirim dari EditManageFragment)
        arguments?.let {
            productId = it.getString("productId")
            binding.etNamaProduk.setText(it.getString("name"))
            binding.etDeskripsi.setText(it.getString("description"))
            binding.etHarga.setText(it.getDouble("price").toString())
            binding.etStok.setText(it.getLong("stock").toString())
            binding.actvKategori.setText(it.getString("category"))
            currentImageUrl = it.getString("imageUrl")

            // Tampilkan gambar
            if (!currentImageUrl.isNullOrEmpty()) {
                binding.ivKategoriPreview.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(currentImageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .into(binding.ivKategoriPreview)
            }
        }

        // Isi dropdown kategori (contoh)
        val kategoriList = listOf("Sneakers", "Sandals", "Boots", "Sports")
        val adapterKategori = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, kategoriList)
        binding.actvKategori.setAdapter(adapterKategori)

        // Pilih gambar baru
        binding.btnPilihGambarBaru.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        // Simpan produk
        binding.btnSimpanProduk.setOnClickListener {
            simpanPerubahan()
        }

        return binding.root
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
            val ref = storage.reference.child("product_images/${System.currentTimeMillis()}.jpg")
            ref.putFile(imageUri!!)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { uri ->
                        updateProduk(nama, deskripsi, harga, stok, kategori, uri.toString())
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal upload gambar", Toast.LENGTH_SHORT).show()
                }
        } else {
            updateProduk(nama, deskripsi, harga, stok, kategori, currentImageUrl ?: "")
        }
    }

    private fun updateProduk(
        nama: String,
        deskripsi: String,
        harga: Double,
        stok: Long,
        kategori: String,
        imageUrl: String
    ) {
        val updateData = mapOf(
            "name" to nama,
            "description" to deskripsi,
            "price" to harga,
            "stock" to stok,
            "category" to kategori,
            "imageUrl" to imageUrl
        )

        firestore.collection("products")
            .document(productId!!)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Produk berhasil diupdate", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal update produk", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
