package com.imersa.warnu.ui.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.imersa.warnu.R
import com.imersa.warnu.databinding.FragmentDetailProductBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import java.text.NumberFormat
import java.util.*

@AndroidEntryPoint
class DetailProductFragment : Fragment() {

    private var _binding: FragmentDetailProductBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var firestore: FirebaseFirestore

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

        // Ambil data produk dari Firestore
        firestore.collection("products")
            .document(productId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "Nama Produk"
                    val price = document.getDouble("price") ?: 0.0
                    val description = document.getString("description") ?: "Deskripsi tidak tersedia"
                    val stock = document.getLong("stock")?.toInt() ?: 0
                    val category = document.getString("category") ?: "Tanpa Kategori"
                    val imageUrl = document.getString("imageUrl")

                    val formattedPrice = NumberFormat.getNumberInstance(Locale("id", "ID")).format(price)

                    binding.tvDetailNamaProduk.text = name
                    binding.tvDetailHargaProduk.text = "Rp$formattedPrice"
                    binding.tvDetailDeskripsi.text = description
                    binding.chipKategori.text = category
                    binding.chipStok.text = "Stok: $stock"

                    Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .into(binding.ivDetailGambarProduk)
                } else {
                    Toast.makeText(requireContext(), "Produk tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal memuat produk", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
